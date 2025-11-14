package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.DoctorService;
import com.ccinfoms17grp2.services.PatientService;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomepageController implements Initializable {

    @FXML private Label greetingLabel;
    @FXML private Label userTypeLabel;
    @FXML private Button signOutButton;
    @FXML private Button viewQueueButton;
    @FXML private Button viewAppointmentsButton;
    @FXML private Button viewConsultationsButton;

    private final User currentUser;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final ExecutorService executorService;
    private final ServiceRegistry services;

    public HomepageController(ServiceRegistry services, User authenticatedUser) {
        this.currentUser = authenticatedUser;
        this.patientService = services.getPatientService();
        this.doctorService = services.getDoctorService();
        this.services = services;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserData();
    }

    private void loadUserData() {
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    String firstName = "";
                    String lastName = "";

                    if (currentUser.getUserType() == User.UserType.PATIENT) {
                        Optional<Patient> patient = patientService.listPatients().stream()
                            .filter(p -> p.getPatientId() == currentUser.getPersonId())
                            .findFirst();
                        if (patient.isPresent()) {
                            firstName = patient.get().getFirstName();
                            lastName = patient.get().getLastName();
                        }
                    } else if (currentUser.getUserType() == User.UserType.DOCTOR) {
                        Optional<Doctor> doctor = doctorService.listDoctors().stream()
                            .filter(d -> d.getDoctorId() == currentUser.getPersonId())
                            .findFirst();
                        if (doctor.isPresent()) {
                            firstName = doctor.get().getFirstName();
                            lastName = doctor.get().getLastName();
                        }
                    }

                    final String finalFirstName = firstName;
                    final String finalLastName = lastName;

                    Platform.runLater(() -> {
                        greetingLabel.setText("Greetings, " + finalFirstName + " " + finalLastName);
                        userTypeLabel.setText("Account Type: " + currentUser.getUserType());
                    });

                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        greetingLabel.setText("Greetings, " + currentUser.getEmail());
                        userTypeLabel.setText("Account Type: " + currentUser.getUserType());
                    });
                }
                return null;
            }
        };

        executorService.execute(loadTask);
    }

    @FXML
    private void handleSignOut() {
        navigateToLogin();
    }

    @FXML
    private void handleViewAppointments() {
        navigateToAppointmentList();
    }

    @FXML
    private void handleViewConsultations() {
        navigateToConsultationList();
    }

    @FXML
    private void handleViewQueue() {
        navigateToQueueList();
    }

    private void navigateToLogin() {
        Stage stage = (Stage) signOutButton.getScene().getWindow();
        try {
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
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to login screen", ex);
        }
    }

    private void navigateToAppointmentList() {
        Stage stage = (Stage) viewAppointmentsButton.getScene().getWindow();
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
            UiUtils.showError("Navigation Error", "Failed to navigate to appointment list screen", ex);
        }
    }

    private void navigateToConsultationList() {
        Stage stage = (Stage) viewConsultationsButton.getScene().getWindow();
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
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to consultation list screen", ex);
        }
    }

    private void navigateToQueueList() {
        Stage stage = (Stage) viewQueueButton.getScene().getWindow();
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
            UiUtils.showError("Navigation Error", "Failed to navigate to queue list screen", ex);
        }
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}