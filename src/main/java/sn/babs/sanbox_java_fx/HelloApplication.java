package sn.babs.sanbox_java_fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sn.babs.sanbox_java_fx.controllers.LoginController;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start (Stage stage) throws IOException {
        LoginController.showLoginView(stage);
    }

    public static void main (String[] args) {
        launch();
    }
}