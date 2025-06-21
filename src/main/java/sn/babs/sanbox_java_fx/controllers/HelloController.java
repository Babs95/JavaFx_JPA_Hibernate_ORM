package sn.babs.sanbox_java_fx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import sn.babs.sanbox_java_fx.HelloApplication;

import java.io.IOException;

public class HelloController {

    public static void showHomeView(Stage stage) throws IOException {
        // Charger la vue HelloApplication
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("SunuEpecerie");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onLogoutButtonClick (ActionEvent event) throws IOException {
        // Obtenir la scène actuelle
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        //Changer de scéne
        LoginController.showLoginView(stage);
    }
}