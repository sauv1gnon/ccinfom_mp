package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Dashboard controller for admin users
 */
public class DashboardController {

    @FXML
    private User currentUser;
    private final ServiceRegistry services;

    public DashboardController(ServiceRegistry services, User user) {
        this.services = services;
        this.currentUser = user;
    }

    public DashboardController() {
        // Default constructor for FXML loading
        this.services = null;
        this.currentUser = null;
    }

    @FXML
    private void handleSignOut() {
        navigateToLogin();
    }

    @FXML
    private void handleViewQueue() {
        navigateToQueueList();
    }

    @FXML
    private void handleViewAppointments() {
        navigateToAppointmentList();
    }

    @FXML
    private void handleViewConsultations() {
        navigateToConsultationList();
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
            
            // Close the current dashboard window
            Stage currentStage = (Stage) getCurrentStage();
            if (currentStage != null) {
                currentStage.close();
            }
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to login screen", ex);
        }
    }

    private void navigateToQueueList() {
        Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/queue-list.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == QueueListController.class) {
                    return new QueueListController(services, currentUser);
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
            stage.setTitle("Queue Management");
            stage.setResizable(false);
            stage.show();
            
            Stage currentStage = (Stage) getCurrentStage();
            if (currentStage != null) {
                currentStage.close();
            }
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to queue list", ex);
        }
    }

    private void navigateToAppointmentList() {
        Stage stage = new Stage();
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
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/ccinfoms17grp2/ui/app.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Appointment Management");
            stage.setResizable(false);
            stage.show();
            
            Stage currentStage = (Stage) getCurrentStage();
            if (currentStage != null) {
                currentStage.close();
            }
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to appointment list", ex);
        }
    }

    private void navigateToConsultationList() {
        Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/consultation-list.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == ConsultationListController.class) {
                    return new ConsultationListController(services, currentUser);
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
            stage.setTitle("Consultation Management");
            stage.setResizable(false);
            stage.show();
            
            Stage currentStage = (Stage) getCurrentStage();
            if (currentStage != null) {
                currentStage.close();
            }
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to consultation list", ex);
        }
    }

    private Stage getCurrentStage() {
        // This would typically be obtained through the FXML injection
        // For now, we'll need to implement proper stage management
        return null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}