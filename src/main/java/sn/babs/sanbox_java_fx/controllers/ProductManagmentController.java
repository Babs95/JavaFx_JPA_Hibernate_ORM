package sn.babs.sanbox_java_fx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import sn.babs.sanbox_java_fx.HelloApplication;
import sn.babs.sanbox_java_fx.entities.Category;
import sn.babs.sanbox_java_fx.entities.Product;
import sn.babs.sanbox_java_fx.services.CategoryServices;
import sn.babs.sanbox_java_fx.services.ProductServices;
import sn.babs.sanbox_java_fx.utils.Utils;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

public class ProductManagmentController implements Initializable {
    // Formulaire
    @FXML private TextField libelleInput;
    @FXML private TextField prixInput;
    @FXML private TextField qteInput;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextArea descriptionInput;

    // Boutons
    @FXML private Button backButton;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button clearFormBtn;
    @FXML private Button searchBtn;

    // Recherche
    @FXML private TextField searchInput;

    // Table
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> codeColumn;
    @FXML private TableColumn<Product, String> libelleColumn;
    @FXML private TableColumn<Product, String> prixColumn;
    @FXML private TableColumn<Product, String> quantiteColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Void> actionColumn;

    private Product selectedProduct = null;
    private final ObservableList<Product> productsList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoriesList = FXCollections.observableArrayList();

    private final ProductServices productServices = new ProductServices();
    private final CategoryServices categoryServices = new CategoryServices();

    private final DecimalFormat priceFormatter = new DecimalFormat("#,##0.00");
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupCategoryComboBox();
        loadCategories();
        loadData();
    }

    public static void showProductView(Stage stage) throws IOException {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("products-managment-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            stage.setTitle("Gestion Produits");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            Utils.showAlert("Erreur","Impossible de retourner à l'accueil.");
            System.err.println("Erreur de navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Obtenir la scène actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            if(stage == null) throw new IOException();

            // Changer de scène vers la vue principale
            HelloController.showHomeView(stage);

        } catch (IOException e) {
            Utils.showAlert("Erreur","Impossible de retourner à l'accueil.");
            System.err.println("Erreur de navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            String libelle = libelleInput.getText().trim();
            double prix = Double.parseDouble(prixInput.getText().trim());
            int quantite = Integer.parseInt(qteInput.getText().trim());
            Category category = categoryComboBox.getValue();
            String description = descriptionInput.getText().trim();

            Product product;
            String successMessage;

            if (selectedProduct == null) {
                // Ajout d'un nouveau produit
                product = new Product();
                product.setLibelle(libelle);
                product.setPrix(prix);
                product.setQuantite(quantite);
                product.setCategory(category);
                product.setDescription(description);

                // Générer un code automatiquement si pas défini
                product.setCode(generateProductCode(libelle));

                productServices.save(product);
                successMessage = "Produit ajouté avec succès.";
            } else {
                // Modification d'un produit existant
                selectedProduct.setLibelle(libelle);
                selectedProduct.setPrix(prix);
                selectedProduct.setQuantite(quantite);
                selectedProduct.setCategory(category);
                selectedProduct.setDescription(description);

                productServices.save(selectedProduct);
                successMessage = "Produit modifié avec succès.";
            }

            Utils.showAlert("Succès", successMessage);
            clearForm();
            loadData();

        } catch (NumberFormatException e) {
            Utils.showAlert("Erreur", "Veuillez saisir des valeurs numériques valides pour le prix et la quantité.");
        } catch (Exception e) {
            String errorMessage = selectedProduct == null ?
                    "Erreur lors de l'ajout du produit." :
                    "Erreur lors de la modification du produit.";
            Utils.showAlert("Erreur", errorMessage);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    private void setupTable() {
        // Configuration des colonnes
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        libelleColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));

        // Colonne prix avec formatage
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));

        // Colonne quantité
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        // Colonne catégorie - afficher le libellé de la catégorie
        categoryColumn.setCellValueFactory(cellData -> {
            Category category = cellData.getValue().getCategory();
            return new javafx.beans.property.SimpleStringProperty(
                    category != null ? category.getLibelle() : "Aucune"
            );
        });

        // Colonne d'actions
        actionColumn.setCellFactory(col -> new TableCell<Product, Void>() {
            private final Button editButton = new Button("✎");
            private final Button deleteButton = new Button("✖");

            {
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 3;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 3;");

                editButton.setOnAction(e -> editProduct(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> deleteProduct(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });

        // Associer les données à la table
        productTable.setItems(productsList);
    }

    private void setupCategoryComboBox() {
        categoryComboBox.setItems(categoriesList);

        // Converter pour afficher le libellé de la catégorie
        categoryComboBox.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getLibelle() : "";
            }

            @Override
            public Category fromString(String string) {
                return categoriesList.stream()
                        .filter(cat -> cat.getLibelle().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Placeholder
        categoryComboBox.setPromptText("Sélectionnez une catégorie");
    }

    private void loadData() {
        try {
            List<Product> products = productServices.getAll();
            productsList.clear();
            productsList.addAll(products);
        } catch (Exception e) {
            Utils.showAlert("Erreur", "Erreur lors du chargement des produits.");
            e.printStackTrace();
        }
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryServices.getAll();
            categoriesList.clear();
            categoriesList.addAll(categories);
        } catch (Exception e) {
            Utils.showAlert("Erreur", "Erreur lors du chargement des catégories.");
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (libelleInput.getText().trim().isEmpty()) {
            errors.append("- Le nom du produit est obligatoire\n");
        }

        if (prixInput.getText().trim().isEmpty()) {
            errors.append("- Le prix est obligatoire\n");
        } else {
            try {
                double prix = Double.parseDouble(prixInput.getText().trim());
                if (prix < 0) {
                    errors.append("- Le prix doit être positif\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Le prix doit être un nombre valide\n");
            }
        }

        if (qteInput.getText().trim().isEmpty()) {
            errors.append("- La quantité est obligatoire\n");
        } else {
            try {
                int quantite = Integer.parseInt(qteInput.getText().trim());
                if (quantite < 0) {
                    errors.append("- La quantité doit être positive\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- La quantité doit être un nombre entier valide\n");
            }
        }

        if (categoryComboBox.getValue() == null) {
            errors.append("- La catégorie est obligatoire\n");
        }

        if (errors.length() > 0) {
            Utils.showAlert("Erreurs de validation", errors.toString());
            return false;
        }

        return true;
    }

    private String generateProductCode(String libelle) {
        // Générer un code simple basé sur le libellé
        String code = libelle.trim().toUpperCase().replaceAll("\\s+", "");
        if (code.length() > 8) {
            code = code.substring(0, 8);
        }

        // Ajouter un suffixe numérique si nécessaire pour éviter les doublons
        String finalCode = code;
        int counter = 1;

        while (isCodeExists(finalCode)) {
            finalCode = code + String.format("%03d", counter);
            counter++;
        }

        return finalCode;
    }

    private boolean isCodeExists(String code) {
        return productsList.stream()
                .anyMatch(product -> product.getCode() != null &&
                        product.getCode().equalsIgnoreCase(code));
    }

    private void editProduct(Product product) {
        if (product != null) {
            selectedProduct = product;
            libelleInput.setText(product.getLibelle());
            prixInput.setText(String.valueOf(product.getPrix()));
            qteInput.setText(String.valueOf(product.getQuantite()));
            categoryComboBox.setValue(product.getCategory());
            descriptionInput.setText(product.getDescription() != null ? product.getDescription() : "");

            addBtn.setText("Modifier");
            addBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: BOLD;");
        }
    }

    private void deleteProduct(Product product) {
        if (product != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmation");
            confirmDialog.setHeaderText("Supprimer le produit");
            confirmDialog.setContentText(String.format(
                    "Êtes-vous sûr de vouloir supprimer le produit \"%s\" ?",
                    product.getLibelle()
            ));

            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        productServices.delete(product.getId());
                        loadData();
                        clearForm();
                        Utils.showAlert("Succès", "Produit supprimé avec succès.");
                    } catch (Exception e) {
                        Utils.showAlert("Erreur", "Erreur lors de la suppression du produit.");
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void clearForm() {
        selectedProduct = null;
        libelleInput.clear();
        prixInput.clear();
        qteInput.clear();
        descriptionInput.clear();
        categoryComboBox.setValue(null);
        searchInput.clear();

        addBtn.setText("Ajouter");
        addBtn.setStyle("-fx-background-color: #0fe741; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: BOLD;");

        // Restaurer la liste complète si on était en mode recherche
        productTable.setItems(productsList);
    }
}
