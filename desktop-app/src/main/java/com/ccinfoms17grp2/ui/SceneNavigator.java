package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneNavigator {
    private final Stage stage;
    private final ServiceRegistry services;
    private final SessionContext session;

    public SceneNavigator(Stage stage, ServiceRegistry services, SessionContext session) {
        this.stage = stage;
        this.services = services;
        this.session = session;
    }

    public void show(UiView view) {
        Parent root = loadRoot(view);
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        applyStyles(scene);
        stage.setTitle(view.getTitle());
        stage.centerOnScreen();
        stage.show();
    }

    private Parent loadRoot(UiView view) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(view.getResourcePath()));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof ViewController) {
                ViewController viewController = (ViewController) controller;
                viewController.setNavigator(this);
                viewController.setServices(services);
                viewController.setSession(session);
                viewController.onDisplay();
            }
            return root;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load view " + view.name(), ex);
        }
    }

    private void applyStyles(Scene scene) {
        URL css = getClass().getResource("/com/ccinfoms17grp2/ui/app.css");
        if (css != null) {
            String stylesheet = css.toExternalForm();
            if (!scene.getStylesheets().contains(stylesheet)) {
                scene.getStylesheets().add(stylesheet);
            }
        }
    }
}
