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
import sn.babs.sanbox_java_fx.HelloApplication;
import sn.babs.sanbox_java_fx.entities.Category;
import sn.babs.sanbox_java_fx.services.CategoryServices;
import sn.babs.sanbox_java_fx.utils.Utils;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {

    // Formulaire
    @FXML private TextField libelleInput;

    // Boutons
    @FXML private Button backButton;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button clearFormBtn;
    @FXML private Button searchBtn;

    // Recherche
    @FXML private TextField searchInput;

    // Table
    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, Long> idColumn;
    @FXML private TableColumn<Category, String> libelleColumn;
    @FXML private TableColumn<Category, Void> actionColumn;

    private Category selectedCategory = null;

    // Données
    private ObservableList<Category> categoriesList = FXCollections.observableArrayList();

    // Service instance - no constructor injection for FXML controllers
    private final CategoryServices categoryServices = new CategoryServices();

    // FXML controllers must have a no-argument constructor
    public CategoryController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadData();
    }

    public static void showCategoryView(Stage stage) throws IOException {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("categories-managment-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            stage.setTitle("Gestion Catégories");
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
        String libelle = libelleInput.getText().trim();

        if (libelle.isEmpty()) {
            Utils.showAlert("Erreur", "Veuillez saisir un libellé.");
            return;
        }

        try {
            Category category;

            if (selectedCategory == null) {
                // Adding new category
                category = new Category();
                category.setLibelle(libelle);
                categoryServices.save(category);
                Utils.showAlert("Succès", "Catégorie ajoutée avec succès.");
            } else {
                // Updating existing category
                selectedCategory.setLibelle(libelle);
                categoryServices.save(selectedCategory);
                Utils.showAlert("Succès", "Catégorie modifiée avec succès.");
            }

            clearForm();
            loadData();

        } catch (Exception e) {
            Utils.showAlert("Erreur", selectedCategory == null ?
                    "Erreur lors de l'ajout de la catégorie." :
                    "Erreur lors de la modification de la catégorie.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchTerm = searchInput.getText().trim();
        if (searchTerm.isEmpty()) {
            loadData();
            categoriesTable.setItems(categoriesList);
        } else {
            System.out.println("Searching for: " + searchTerm);
            ObservableList<Category> filteredList = categoriesList.filtered(category ->
                    category.getLibelle().toLowerCase().contains(searchTerm)
            );
            categoriesTable.setItems(filteredList);
        }
    }

    private void setupTable() {
        // Configuration des colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        libelleColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));

        // Définir les largeurs des colonnes
        idColumn.setPrefWidth(80);
        libelleColumn.setPrefWidth(200);
        actionColumn.setPrefWidth(120);

        // Colonne d'actions
        actionColumn.setCellFactory(col -> new TableCell<Category, Void>() {
            private final Button editButton = new Button("✎");
            private final Button deleteButton = new Button("✖");

            {
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 3;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 3;");

                editButton.setOnAction(e -> editCategory(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> deleteCategory(getTableView().getItems().get(getIndex())));
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
        categoriesTable.setItems(categoriesList);
    }

    private void loadData() {
        try {
            List<Category> categories = categoryServices.getAll();
            categoriesList.clear();
            categoriesList.addAll(categories);
        } catch (Exception e) {
            Utils.showAlert("Erreur", "Erreur lors du chargement des catégories.");
            e.printStackTrace();
        }
    }

    private void editCategory(Category category) {
        if (category != null) {
            selectedCategory = category;
            libelleInput.setText(category.getLibelle());
            addBtn.setText("Modifier");
            addBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: BOLD;");
        }
    }

    private void deleteCategory(Category category) {
        if (category != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmation");
            confirmDialog.setHeaderText("Supprimer la catégorie");
            confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette catégorie ?");

            if (confirmDialog.showAndWait().get() == ButtonType.OK) {
                try {
                    categoryServices.delete(category.getId());
                    loadData();
                    clearForm();
                    Utils.showAlert("Succès", "Catégorie supprimée avec succès.");
                } catch (Exception e) {
                    Utils.showAlert("Erreur", "Erreur lors de la suppression de la catégorie.");
                    e.printStackTrace();
                }
            }
        }
    }

    private void clearForm() {
        selectedCategory = null;
        libelleInput.clear();
        searchInput.clear();
        addBtn.setText("Ajouter");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: BOLD;");
    }
}
