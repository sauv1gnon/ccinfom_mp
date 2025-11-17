package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.PatientService;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.ValidationException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PatientFormController implements Initializable {

    @FXML
    private TextField lastNameField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField contactField;
    @FXML
    private TextField emailField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label titleLabel;

    private final ServiceRegistry services;
    private final User currentUser;
    private final Patient editingPatient;
    private final ExecutorService executorService;

    public PatientFormController(ServiceRegistry services, User currentUser, Patient editingPatient) {
        this.services = services;
        this.currentUser = currentUser;
        this.editingPatient = editingPatient;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (editingPatient != null) {
            titleLabel.setText("Edit Patient");
            populateForm();
        } else {
            titleLabel.setText("Create Patient");
        }
    }

    private void populateForm() {
        lastNameField.setText(editingPatient.getLastName());
        firstNameField.setText(editingPatient.getFirstName());
        contactField.setText(editingPatient.getContactNumber());
        emailField.setText(editingPatient.getEmail());
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                PatientService patientService = services.getPatientService();
                
                if (editingPatient == null) {
                    Patient newPatient = new Patient();
                    newPatient.setLastName(lastNameField.getText().trim());
                    newPatient.setFirstName(firstNameField.getText().trim());
                    newPatient.setContactNumber(contactField.getText().trim());
                    newPatient.setEmail(emailField.getText().trim());
                    
                    patientService.createPatient(newPatient);
                } else {
                    editingPatient.setLastName(lastNameField.getText().trim());
                    editingPatient.setFirstName(firstNameField.getText().trim());
                    editingPatient.setContactNumber(contactField.getText().trim());
                    editingPatient.setEmail(emailField.getText().trim());
                    
                    patientService.updatePatient(editingPatient);
                }
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            UiUtils.showInfo("Success", editingPatient == null ? "Patient created successfully" : "Patient updated successfully");
            closeForm();
        });
        
        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            if (ex instanceof ValidationException) {
                UiUtils.showError("Validation Error", ex.getMessage(), null);
            } else {
                UiUtils.showError("Error", "Failed to save patient", ex);
            }
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private boolean validateForm() {
        if (lastNameField.getText().trim().isEmpty()) {
            UiUtils.showError("Validation Error", "Last name is required", null);
            return false;
        }
        if (firstNameField.getText().trim().isEmpty()) {
            UiUtils.showError("Validation Error", "First name is required", null);
            return false;
        }
        if (contactField.getText().trim().isEmpty()) {
            UiUtils.showError("Validation Error", "Contact number is required", null);
            return false;
        }
        return true;
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
