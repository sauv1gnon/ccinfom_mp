package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.BranchService;
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

public class BranchFormController implements Initializable {

    @FXML
    private TextField branchNameField;
    @FXML
    private TextArea addressField;
    @FXML
    private TextField latitudeField;
    @FXML
    private TextField longitudeField;
    @FXML
    private TextField capacityField;
    @FXML
    private TextField contactField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label titleLabel;

    private final ServiceRegistry services;
    private final User currentUser;
    private final Branch editingBranch;
    private final ExecutorService executorService;

    public BranchFormController(ServiceRegistry services, User currentUser, Branch editingBranch) {
        this.services = services;
        this.currentUser = currentUser;
        this.editingBranch = editingBranch;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (editingBranch != null) {
            titleLabel.setText("Edit Branch");
            populateForm();
        } else {
            titleLabel.setText("Create Branch");
        }
    }

    private void populateForm() {
        branchNameField.setText(editingBranch.getBranchName());
        addressField.setText(editingBranch.getAddress());
        latitudeField.setText(String.valueOf(editingBranch.getLatitude()));
        longitudeField.setText(String.valueOf(editingBranch.getLongitude()));
        capacityField.setText(String.valueOf(editingBranch.getCapacity()));
        contactField.setText(editingBranch.getContactNumber());
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                BranchService branchService = services.getBranchService();
                
                if (editingBranch == null) {
                    Branch newBranch = new Branch();
                    newBranch.setBranchName(branchNameField.getText().trim());
                    newBranch.setAddress(addressField.getText().trim());
                    newBranch.setLatitude(parseDouble(latitudeField.getText().trim(), 0.0));
                    newBranch.setLongitude(parseDouble(longitudeField.getText().trim(), 0.0));
                    newBranch.setCapacity(Integer.parseInt(capacityField.getText().trim()));
                    newBranch.setContactNumber(contactField.getText().trim());
                    
                    branchService.createBranch(newBranch);
                } else {
                    editingBranch.setBranchName(branchNameField.getText().trim());
                    editingBranch.setAddress(addressField.getText().trim());
                    editingBranch.setLatitude(parseDouble(latitudeField.getText().trim(), editingBranch.getLatitude()));
                    editingBranch.setLongitude(parseDouble(longitudeField.getText().trim(), editingBranch.getLongitude()));
                    editingBranch.setCapacity(Integer.parseInt(capacityField.getText().trim()));
                    editingBranch.setContactNumber(contactField.getText().trim());
                    
                    branchService.updateBranch(editingBranch);
                }
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            UiUtils.showInfo("Success", editingBranch == null ? "Branch created successfully" : "Branch updated successfully");
            closeForm();
        });
        
        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            if (ex instanceof ValidationException) {
                UiUtils.showError("Validation Error", ex.getMessage(), null);
            } else {
                UiUtils.showError("Error", "Failed to save branch", ex);
            }
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private boolean validateForm() {
        if (branchNameField.getText().trim().isEmpty()) {
            UiUtils.showError("Validation Error", "Branch name is required", null);
            return false;
        }
        if (addressField.getText().trim().isEmpty()) {
            UiUtils.showError("Validation Error", "Address is required", null);
            return false;
        }
        if (capacityField.getText().trim().isEmpty()) {
            UiUtils.showError("Validation Error", "Capacity is required", null);
            return false;
        }
        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            if (capacity <= 0) {
                UiUtils.showError("Validation Error", "Capacity must be greater than 0", null);
                return false;
            }
        } catch (NumberFormatException e) {
            UiUtils.showError("Validation Error", "Capacity must be a valid number", null);
            return false;
        }
        if (contactField.getText().trim().isEmpty()) {
            UiUtils.showError("Validation Error", "Contact number is required", null);
            return false;
        }
        return true;
    }

    private double parseDouble(String value, double defaultValue) {
        try {
            return value.isEmpty() ? defaultValue : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
