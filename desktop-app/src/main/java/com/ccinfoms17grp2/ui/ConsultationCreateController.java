package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ConsultationCreateController implements Initializable {

    @FXML private TextField appointmentIdField;
    @FXML private TextArea diagnosisField;
    @FXML private TextArea treatmentPlanField;
    @FXML private TextArea prescriptionField;
    @FXML private DatePicker followUpDatePicker;

    private final User currentUser;
    private final ServiceRegistry services;

    public ConsultationCreateController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        followUpDatePicker.setValue(LocalDate.now().plusDays(7));
    }

    @FXML
    private void handleCancelCreate() {
        navigateToAppointmentList();
    }

    @FXML
    private void handleBackToHomepage() {
        navigateToHomepage();
    }

    @FXML
    private void handleConfirmCreate() {
        navigateToAppointmentConfirmed();
    }

    private void navigateToAppointmentList() {
        Stage stage = (Stage) appointmentIdField.getScene().getWindow();
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

    private void navigateToHomepage() {
        Stage stage = (Stage) appointmentIdField.getScene().getWindow();
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

    private void navigateToAppointmentConfirmed() {
        Stage stage = (Stage) appointmentIdField.getScene().getWindow();
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
}