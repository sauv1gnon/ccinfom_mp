package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.*;
import com.ccinfoms17grp2.services.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueueFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private ComboBox<Patient> patientComboBox;
    @FXML private ComboBox<Branch> branchComboBox;
    @FXML private TextField queueNumberField;
    @FXML private ComboBox<QueueStatus> statusComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final User currentUser;
    private final ServiceRegistry services;
    private final Queue queue;
    private final ExecutorService executorService;
    private final ObservableList<Patient> patients;
    private final ObservableList<Branch> branches;

    public QueueFormController(ServiceRegistry services, User currentUser, Queue queue) {
        this.services = services;
        this.currentUser = currentUser;
        this.queue = queue;
        this.executorService = Executors.newCachedThreadPool();
        this.patients = FXCollections.observableArrayList();
        this.branches = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleLabel.setText(queue == null ? "Create Queue Entry" : "Edit Queue Entry");
        
        statusComboBox.getItems().addAll(QueueStatus.values());
        
        if (queue == null) {
            queueNumberField.setEditable(false);
            queueNumberField.setPromptText("Auto-assigned");
            statusComboBox.setValue(QueueStatus.WAITING);
        }
        
        loadPatients();
        loadBranches();
        
        if (queue != null) {
            populateForm();
        }
    }

    private void loadPatients() {
        Task<List<Patient>> task = new Task<>() {
            @Override
            protected List<Patient> call() {
                return services.getPatientService().listPatients();
            }
        };
        
        task.setOnSucceeded(event -> {
            patients.clear();
            patients.addAll(task.getValue());
            patientComboBox.setItems(patients);
            
            if (queue != null) {
                patients.stream()
                    .filter(p -> p.getPatientId() == queue.getPatientId())
                    .findFirst()
                    .ifPresent(patientComboBox::setValue);
            }
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load patients", task.getException());
        });
        
        executorService.execute(task);
    }

    private void loadBranches() {
        Task<List<Branch>> task = new Task<>() {
            @Override
            protected List<Branch> call() {
                return services.getBranchService().listBranches();
            }
        };
        
        task.setOnSucceeded(event -> {
            branches.clear();
            branches.addAll(task.getValue());
            branchComboBox.setItems(branches);
            
            if (queue != null) {
                branches.stream()
                    .filter(b -> b.getBranchId() == queue.getBranchId())
                    .findFirst()
                    .ifPresent(branchComboBox::setValue);
            }
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load branches", task.getException());
        });
        
        executorService.execute(task);
    }

    private void populateForm() {
        queueNumberField.setText(String.valueOf(queue.getQueueNumber()));
        queueNumberField.setEditable(false);
        statusComboBox.setValue(queue.getStatus());
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }
        
        Task<Queue> saveTask = new Task<>() {
            @Override
            protected Queue call() throws Exception {
                Patient selectedPatient = patientComboBox.getValue();
                Branch selectedBranch = branchComboBox.getValue();
                QueueStatus selectedStatus = statusComboBox.getValue();
                
                if (queue == null) {
                    Queue newQueue = new Queue(
                        0,
                        selectedPatient.getPatientId(),
                        selectedBranch.getBranchId(),
                        0,
                        selectedStatus,
                        null
                    );
                    return services.getQueueService().createQueue(newQueue);
                } else {
                    queue.setPatientId(selectedPatient.getPatientId());
                    queue.setBranchId(selectedBranch.getBranchId());
                    queue.setStatus(selectedStatus);
                    return services.getQueueService().updateQueue(queue);
                }
            }
        };
        
        saveTask.setOnSucceeded(event -> {
            UiUtils.showInformation("Success", 
                queue == null ? "Queue entry created successfully" : "Queue entry updated successfully");
            navigateBack();
        });
        
        saveTask.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to save queue entry", saveTask.getException());
        });
        
        executorService.execute(saveTask);
    }

    private boolean validateForm() {
        if (patientComboBox.getValue() == null) {
            UiUtils.showWarning("Validation Error", "Please select a patient");
            return false;
        }
        
        if (branchComboBox.getValue() == null) {
            UiUtils.showWarning("Validation Error", "Please select a branch");
            return false;
        }
        
        if (statusComboBox.getValue() == null) {
            UiUtils.showWarning("Validation Error", "Please select a status");
            return false;
        }
        
        return true;
    }

    @FXML
    private void handleCancel() {
        navigateBack();
    }

    private void navigateBack() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/queue-list.fxml"));
            loader.setControllerFactory(cls -> 
                cls == QueueListController.class ? new QueueListController(services, currentUser) : null
            );
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();
            cleanup();
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate back", ex);
        }
    }

    private void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
