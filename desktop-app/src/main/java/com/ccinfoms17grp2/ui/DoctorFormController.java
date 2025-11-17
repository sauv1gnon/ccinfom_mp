package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.DoctorAvailabilityStatus;
import com.ccinfoms17grp2.models.Specialization;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.DoctorService;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.SpecializationService;
import com.ccinfoms17grp2.services.ValidationException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DoctorFormController implements Initializable {

    @FXML
    private TextField lastNameField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<DoctorAvailabilityStatus> statusComboBox;
    @FXML
    private ListView<Specialization> specializationListView;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label titleLabel;

    private final ServiceRegistry services;
    private final User currentUser;
    private final Doctor editingDoctor;
    private final ExecutorService executorService;
    private List<Specialization> allSpecializations;

    public DoctorFormController(ServiceRegistry services, User currentUser, Doctor editingDoctor) {
        this.services = services;
        this.currentUser = currentUser;
        this.editingDoctor = editingDoctor;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusComboBox.getItems().addAll(DoctorAvailabilityStatus.values());
        specializationListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        loadSpecializations();
        
        if (editingDoctor != null) {
            titleLabel.setText("Edit Doctor");
        } else {
            titleLabel.setText("Create Doctor");
            statusComboBox.setValue(DoctorAvailabilityStatus.AVAILABLE);
        }
    }

    private void loadSpecializations() {
        Task<List<Specialization>> task = new Task<>() {
            @Override
            protected List<Specialization> call() {
                return services.getSpecializationService().listSpecializations();
            }
        };
        
        task.setOnSucceeded(event -> {
            allSpecializations = task.getValue();
            specializationListView.getItems().addAll(allSpecializations);
            
            if (editingDoctor != null) {
                populateForm();
            }
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load specializations", task.getException());
        });
        
        executorService.execute(task);
    }

    private void populateForm() {
        lastNameField.setText(editingDoctor.getLastName());
        firstNameField.setText(editingDoctor.getFirstName());
        emailField.setText(editingDoctor.getEmail());
        statusComboBox.setValue(editingDoctor.getAvailabilityStatus());
        
        // Select specializations
        List<Integer> doctorSpecIds = editingDoctor.getSpecializationIds();
        if (doctorSpecIds != null) {
            for (int i = 0; i < allSpecializations.size(); i++) {
                if (doctorSpecIds.contains(allSpecializations.get(i).getSpecializationId())) {
                    specializationListView.getSelectionModel().select(i);
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                DoctorService doctorService = services.getDoctorService();
                
                List<Integer> selectedSpecIds = specializationListView.getSelectionModel()
                    .getSelectedItems()
                    .stream()
                    .map(Specialization::getSpecializationId)
                    .collect(Collectors.toList());
                
                if (editingDoctor == null) {
                    Doctor newDoctor = new Doctor();
                    newDoctor.setLastName(lastNameField.getText().trim());
                    newDoctor.setFirstName(firstNameField.getText().trim());
                    newDoctor.setEmail(emailField.getText().trim());
                    newDoctor.setAvailabilityStatus(statusComboBox.getValue());
                    newDoctor.setSpecializationIds(selectedSpecIds);
                    
                    doctorService.createDoctor(newDoctor);
                } else {
                    editingDoctor.setLastName(lastNameField.getText().trim());
                    editingDoctor.setFirstName(firstNameField.getText().trim());
                    editingDoctor.setEmail(emailField.getText().trim());
                    editingDoctor.setAvailabilityStatus(statusComboBox.getValue());
                    editingDoctor.setSpecializationIds(selectedSpecIds);
                    
                    doctorService.updateDoctor(editingDoctor);
                }
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            UiUtils.showInfo("Success", editingDoctor == null ? "Doctor created successfully" : "Doctor updated successfully");
            closeForm();
        });
        
        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            if (ex instanceof ValidationException) {
                UiUtils.showError("Validation Error", ex.getMessage(), null);
            } else {
                UiUtils.showError("Error", "Failed to save doctor", ex);
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
        if (emailField.getText().trim().isEmpty()) {
            UiUtils.showError("Validation Error", "Email is required", null);
            return false;
        }
        if (statusComboBox.getValue() == null) {
            UiUtils.showError("Validation Error", "Availability status is required", null);
            return false;
        }
        if (specializationListView.getSelectionModel().getSelectedItems().isEmpty()) {
            UiUtils.showError("Validation Error", "At least one specialization is required", null);
            return false;
        }
        return true;
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
