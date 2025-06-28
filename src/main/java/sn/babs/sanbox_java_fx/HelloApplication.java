package sn.babs.sanbox_java_fx;

import jakarta.persistence.EntityManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sn.babs.sanbox_java_fx.controllers.HelloController;
import sn.babs.sanbox_java_fx.controllers.LoginController;
import sn.babs.sanbox_java_fx.services.UserServices;
import sn.babs.sanbox_java_fx.utils.JPAUtils;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start (Stage stage) throws IOException {
        //LoginController.showLoginView(stage);
        HelloController.showHomeView(stage);
    }

    @Override
    public void init() throws Exception {
        super.init();
        initializeDefaultAdmin();
    }

    public static void main (String[] args) {
        launch();
    }

    /**
     * Initialise l'utilisateur admin par défaut
     */
    private void initializeDefaultAdmin() {
        try {
            UserServices userServices = new UserServices();
            userServices.createDefaultAdminIfNotExists();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de l'admin : " + e.getMessage());
            e.printStackTrace();
        }
    }
}