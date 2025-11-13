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
import javafx.scene.control.Label;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomepageController implements Initializable {

    @FXML private Label greetingLabel;
    @FXML private Label userTypeLabel;

    private final User currentUser;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final ExecutorService executorService;

    public HomepageController(ServiceRegistry services, User authenticatedUser) {
        this.currentUser = authenticatedUser;
        this.patientService = services.getPatientService();
        this.doctorService = services.getDoctorService();
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

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}