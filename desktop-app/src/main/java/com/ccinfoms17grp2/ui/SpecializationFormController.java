package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Specialization;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.SpecializationService;
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

public class SpecializationFormController implements Initializable {

    @FXML
    private TextField nameField;
    @FXML
    private TextField codeField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label titleLabel;

    private final ServiceRegistry services;
    private final User currentUser;
    private final Specialization editingSpecialization;
    private final ExecutorService executorService;

    public SpecializationFormController(ServiceRegistry services, User currentUser, Specialization editingSpecialization) {
        this.services = services;
        this.currentUser = currentUser;
        this.editingSpecialization = editingSpecialization;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (editingSpecialization != null) {
            titleLabel.setText("Edit Specialization");
            populateForm();
        } else {
            titleLabel.setText("Create Specialization");
        }
    }

    private void populateForm() {
        nameField.setText(editingSpecialization.getSpecializationName());
        codeField.setText(editingSpecialization.getSpecializationCode());
        descriptionField.setText(editingSpecialization.getDescription());
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                SpecializationService specializationService = services.getSpecializationService();
                
                if (editingSpecialization == null) {
                    Specialization newSpecialization = new Specialization();
                    newSpecialization.setSpecializationName(nameField.getText().trim());
                    newSpecialization.setSpecializationCode(codeField.getText().trim().toUpperCase());
                    newSpecialization.setDescription(descriptionField.getText().trim());
                    
                    specializationService.createSpecialization(newSpecialization);
                } else {
                    editingSpecialization.setSpecializationName(nameField.getText().trim());
                    editingSpecialization.setSpecializationCode(codeField.getText().trim().toUpperCase());
                    editingSpecialization.setDescription(descriptionField.getText().trim());
                    
                    specializationService.updateSpecialization(editingSpecialization);
                }
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            UiUtils.showInfo("Success", editingSpecialization == null ? "Specialization created successfully" : "Specialization updated successfully");
            closeForm();
        });
        
        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            if (ex instanceof ValidationException) {
                UiUtils.showError("Validation Error", ex.getMessage(), null);
            } else {
                UiUtils.showError("Error", "Failed to save specialization", ex);
            }
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            UiUtils.showError("Validation Error", "Specialization name is required", null);
            return false;
        }
        if (codeField.getText().trim().isEmpty()) {
            UiUtils.showError("Validation Error", "Specialization code is required", null);
            return false;
        }
        return true;
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
