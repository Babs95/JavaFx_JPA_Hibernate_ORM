package sn.babs.sanbox_java_fx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sn.babs.sanbox_java_fx.HelloApplication;
import sn.babs.sanbox_java_fx.entities.User;
import sn.babs.sanbox_java_fx.services.UserServices;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginBtn;

    // Instance du service utilisateur
    private final UserServices userServices;

    public LoginController() {
        this.userServices = new UserServices();
    }

    public static void showLoginView(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 350, 400);

        stage.setTitle("Connexion - SunuEpicerie");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        // Masquer le message d'erreur précédent
        errorLabel.setVisible(false);

        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            // Tentative d'authentification
            User authenticatedUser = userServices.authenticate(email, password);

            if (authenticatedUser != null) {
                // Connexion réussie
                System.out.println("Connexion réussie pour : " + authenticatedUser.getEmail());
                navigateToHomeView(event);
            } else {
                // Échec de l'authentification
                showError("Email ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            // Erreur lors de l'authentification
            showError("Erreur lors de la connexion. Veuillez réessayer.");
            System.err.println("Erreur d'authentification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Navigue vers la vue principale après connexion réussie
     */
    private void navigateToHomeView(ActionEvent event) {
        try {
            // Obtenir la scène actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Changer de scène vers la vue principale
            HelloController.showHomeView(stage);

        } catch (IOException e) {
            showError("Erreur lors du chargement de la page principale.");
            System.err.println("Erreur de navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}