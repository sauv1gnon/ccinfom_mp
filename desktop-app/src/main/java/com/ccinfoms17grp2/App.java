package com.ccinfoms17grp2;

import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.ui.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private ServiceRegistry services;

    @Override
    public void start(Stage primaryStage) throws IOException {
        services = new ServiceRegistry();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/login.fxml"));
        
        loader.setControllerFactory(controllerClass -> {
            if (controllerClass == LoginController.class) {
                return new LoginController(services);
            }
            try {
                return controllerClass.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
            }
        });

        Parent root = loader.load();
        
        // Style Sheet
        // Reuse as much as possible
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/ccinfoms17grp2/ui/app.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Digital Queue and Appointment System");
        primaryStage.setResizable(false);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (services != null) {
            // TODO: Implement cleanup for services
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
