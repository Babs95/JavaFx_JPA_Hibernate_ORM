package sn.babs.sanbox_java_fx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sn.babs.sanbox_java_fx.HelloApplication;

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

    private static final String VALID_EMAIL = "isi25@yopmail.com";
    private static final String VALID_PASSWORD = "passer@12";

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
        errorLabel.setVisible(false);

        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        //Validation des champs
        if(email.isEmpty() || password.isEmpty()){
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if(VALID_EMAIL.equals(email) && VALID_PASSWORD.equals(password)) {
            try{
                navigateToHelloApplication(event);
            }catch (IOException e) {
                showError("Erreur lors du chargement de la page principale");
                e.printStackTrace();
            }
        }else showError("Email ou mot de passe incorrect.");


    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void navigateToHelloApplication(ActionEvent event) throws IOException {
        // Obtenir la scène actuelle
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        //Changer de scéne
        HelloController.showHomeView(stage);
    }
}
