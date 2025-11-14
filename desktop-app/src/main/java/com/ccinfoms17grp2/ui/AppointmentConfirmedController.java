package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.*;
import com.ccinfoms17grp2.services.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.util.ResourceBundle;

public class AppointmentConfirmedController implements Initializable {

    private final User currentUser;
    private final ServiceRegistry services;

    public AppointmentConfirmedController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    private void handleReturnToHomepage() {
        navigateToHomepage();
    }

    private void navigateToHomepage() {
        Stage stage = (Stage) Stage.getWindows().stream()
            .filter(window -> window instanceof Stage && ((Stage) window).isShowing())
            .map(window -> (Stage) window)
            .findFirst()
            .orElse(null);
            
        if (stage == null) {
            return;
        }
            
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/homepage.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == HomepageController.class) {
                    return new HomepageController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to homepage", ex);
        }
    }
}