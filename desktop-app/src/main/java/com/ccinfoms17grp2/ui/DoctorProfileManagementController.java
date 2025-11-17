package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.DoctorAvailabilityStatus;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.DoctorService;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.ValidationException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorProfileManagementController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<DoctorAvailabilityStatus> availabilityStatusComboBox;
    @FXML private TextArea availabilityRangesTextArea;
    
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private final User currentUser;
    private final ServiceRegistry services;
    private final DoctorService doctorService;
    private final ExecutorService executorService;
    private Doctor currentDoctor;

    public DoctorProfileManagementController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.doctorService = services.getDoctorService();
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleLabel.setText("My Profile");
        
        // Setup availability status combo box
        availabilityStatusComboBox.setItems(FXCollections.observableArrayList(DoctorAvailabilityStatus.values()));
        
        loadDoctorProfile();
    }
    
    private void loadDoctorProfile() {
        Task<Doctor> loadTask = new Task<>() {
            @Override
            protected Doctor call() throws Exception {
                int doctorId = currentUser.getPersonId();
                Optional<Doctor> doctor = doctorService.getDoctorById(doctorId);
                if (doctor.isEmpty()) {
                    throw new Exception("Doctor profile not found");
                }
                return doctor.get();
            }
            
            @Override
            protected void succeeded() {
                currentDoctor = getValue();
                populateForm();
            }
            
            @Override
            protected void failed() {
                UiUtils.showError("Failed to load profile: " + getException().getMessage());
            }
        };
        
        executorService.execute(loadTask);
    }
    
    private void populateForm() {
        firstNameField.setText(currentDoctor.getFirstName());
        lastNameField.setText(currentDoctor.getLastName());
        emailField.setText(currentDoctor.getEmail() != null ? currentDoctor.getEmail() : "");
        availabilityStatusComboBox.setValue(currentDoctor.getAvailabilityStatus());
        
        // Display availability ranges (JSON string from DB)
        // Could be enhanced with a proper date-time range picker
        if (currentDoctor.getAvailabilityDatetimeRanges() != null && !currentDoctor.getAvailabilityDatetimeRanges().isEmpty()) {
            availabilityRangesTextArea.setText(currentDoctor.getAvailabilityDatetimeRanges());
        } else {
            availabilityRangesTextArea.setText("[]");
            availabilityRangesTextArea.setPromptText("Enter availability ranges in JSON format, e.g., [{\"start\": \"2025-11-15T09:00\", \"end\": \"2025-11-15T17:00\"}]");
        }
    }
    
    @FXML
    private void handleSave() {
        try {
            // Validate
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            DoctorAvailabilityStatus status = availabilityStatusComboBox.getValue();
            String availabilityRanges = availabilityRangesTextArea.getText();
            
            if (firstName == null || firstName.trim().isEmpty()) {
                UiUtils.showError("First name is required");
                return;
            }
            
            if (lastName == null || lastName.trim().isEmpty()) {
                UiUtils.showError("Last name is required");
                return;
            }
            
            if (status == null) {
                UiUtils.showError("Availability status is required");
                return;
            }
            
            if (availabilityRanges != null && !availabilityRanges.trim().isEmpty()) {
                String trimmed = availabilityRanges.trim();
                if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
                    UiUtils.showError("Availability ranges must be in JSON array format");
                    return;
                }
            }
            
            currentDoctor.setFirstName(firstName.trim());
            currentDoctor.setLastName(lastName.trim());
            currentDoctor.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);
            currentDoctor.setAvailabilityStatus(status);
            currentDoctor.setAvailabilityDatetimeRanges(availabilityRanges != null && !availabilityRanges.trim().isEmpty() ? availabilityRanges.trim() : null);
            
            // Save
            Task<Doctor> saveTask = new Task<>() {
                @Override
                protected Doctor call() throws Exception {
                    return doctorService.updateDoctor(currentDoctor);
                }
                
                @Override
                protected void succeeded() {
                    UiUtils.showInfo("Profile updated successfully");
                    handleCancel();
                }
                
                @Override
                protected void failed() {
                    Throwable ex = getException();
                    if (ex instanceof ValidationException) {
                        UiUtils.showError(ex.getMessage());
                    } else {
                        UiUtils.showError("Failed to update profile: " + ex.getMessage());
                    }
                }
            };
            
            executorService.execute(saveTask);
            
        } catch (Exception e) {
            UiUtils.showError("Invalid input: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        try {
            executorService.shutdownNow();
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/doctor-dashboard.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == DoctorDashboardController.class) {
                    return new DoctorDashboardController(services, currentUser);
                }
                throw new IllegalStateException("Unexpected controller: " + controllerClass);
            });
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            UiUtils.showError("Navigation failed: " + e.getMessage());
        }
    }
}
