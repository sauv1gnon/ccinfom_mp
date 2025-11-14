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

public class BookAppointmentController implements Initializable {

    @FXML private Button cancelButton;
    @FXML private Button bookButton;
    
    private final User currentUser;
    private final ServiceRegistry services;

    public BookAppointmentController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    private void handleCancelBooking() {
        navigateToAppointmentList();
    }

    @FXML
    private void handleBookAppointment() {
        navigateToAppointmentConfirmed();
    }

    @FXML
    private void handleBackToHomepage() {
        navigateToHomepage();
    }

    private void navigateToAppointmentList() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/appointment-list.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AppointmentListController.class) {
                    return new AppointmentListController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to appointment list", ex);
        }
    }

    private void navigateToAppointmentConfirmed() {
        Stage stage = (Stage) bookButton.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/appointment-confirmed.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AppointmentConfirmedController.class) {
                    return new AppointmentConfirmedController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to appointment confirmed", ex);
        }
    }

    private void navigateToHomepage() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
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