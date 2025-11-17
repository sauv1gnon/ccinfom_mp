package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Admin Dashboard controller with navigation to all management modules
 */
public class AdminDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Button signOutButton;

    private final ServiceRegistry services;
    private final User currentUser;

    public AdminDashboardController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, Admin");
        }
    }

    @FXML
    private void handleSignOut() {
        navigateToLogin();
    }

    @FXML
    private void handleManageUsers() {
        navigateTo("/com/ccinfoms17grp2/ui/user-list.fxml", 
                   UserListController.class, 
                   "User Management",
                   () -> new UserListController(services, currentUser));
    }

    @FXML
    private void handleManagePatients() {
        navigateTo("/com/ccinfoms17grp2/ui/patient-list.fxml", 
                   PatientListController.class, 
                   "Patient Management",
                   () -> new PatientListController(services, currentUser));
    }

    @FXML
    private void handleManageDoctors() {
        navigateTo("/com/ccinfoms17grp2/ui/doctor-list.fxml", 
                   DoctorListController.class, 
                   "Doctor Management",
                   () -> new DoctorListController(services, currentUser));
    }

    @FXML
    private void handleManageBranches() {
        navigateTo("/com/ccinfoms17grp2/ui/branch-list.fxml", 
                   BranchListController.class, 
                   "Branch Management",
                   () -> new BranchListController(services, currentUser));
    }

    @FXML
    private void handleManageSpecializations() {
        navigateTo("/com/ccinfoms17grp2/ui/specialization-list.fxml", 
                   SpecializationListController.class, 
                   "Specialization Management",
                   () -> new SpecializationListController(services, currentUser));
    }

    @FXML
    private void handleManageAppointments() {
        navigateTo("/com/ccinfoms17grp2/ui/appointment-list.fxml", 
                   AppointmentListController.class, 
                   "Appointment Management",
                   () -> new AppointmentListController(services, currentUser));
    }

    @FXML
    private void handleManageConsultations() {
        navigateTo("/com/ccinfoms17grp2/ui/consultation-list.fxml", 
                   ConsultationListController.class, 
                   "Consultation Management",
                   () -> new ConsultationListController(services, currentUser));
    }

    @FXML
    private void handleManageQueues() {
        navigateTo("/com/ccinfoms17grp2/ui/queue-list.fxml", 
                   QueueListController.class, 
                   "Queue Management",
                   () -> new QueueListController(services, currentUser));
    }

    @SuppressWarnings("UseSpecificCatch")
    private void navigateToLogin() {
        Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/login.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == LoginController.class) {
                    return new LoginController(new ServiceRegistry());
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/ccinfoms17grp2/ui/app.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Digital Queue and Appointment System");
            stage.setResizable(false);
            stage.show();
            
            closeCurrentStage();
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to login screen", ex);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    private <T> void navigateTo(String fxmlPath, Class<T> controllerClass, String title, ControllerSupplier<T> supplier) {
        Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(clazz -> {
                if (clazz == controllerClass) {
                    return supplier.get();
                }
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + clazz, ex);
                }
            });
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/ccinfoms17grp2/ui/app.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(true);
            stage.show();
            
            closeCurrentStage();
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to " + title, ex);
        }
    }

    private void closeCurrentStage() {
        Stage currentStage = (Stage) signOutButton.getScene().getWindow();
        if (currentStage != null) {
            currentStage.close();
        }
    }

    @FunctionalInterface
    private interface ControllerSupplier<T> {
        T get();
    }
}
