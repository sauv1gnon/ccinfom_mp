package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.models.Queue;
import com.ccinfoms17grp2.models.QueueStatus;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.BranchService;
import com.ccinfoms17grp2.services.PatientService;
import com.ccinfoms17grp2.services.QueueService;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.utils.DateTimeUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorQueueManagementController implements Initializable {

    @FXML private TableView<Queue> queueTable;
    @FXML private TableColumn<Queue, Integer> queueIdColumn;
    @FXML private TableColumn<Queue, Integer> queueNumberColumn;
    @FXML private TableColumn<Queue, String> patientNameColumn;
    @FXML private TableColumn<Queue, String> branchColumn;
    @FXML private TableColumn<Queue, String> statusColumn;
    @FXML private TableColumn<Queue, String> createdAtColumn;
    
    @FXML private ComboBox<Branch> branchFilterComboBox;
    @FXML private ComboBox<QueueStatus> statusFilterComboBox;
    @FXML private CheckBox todayOnlyCheckBox;
    
    @FXML private Button callNextButton;
    @FXML private Button markServedButton;
    @FXML private Button markNoShowButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    
    private final User currentUser;
    private final ServiceRegistry services;
    private final QueueService queueService;
    private final BranchService branchService;
    private final PatientService patientService;
    private final ExecutorService executorService;
    
    private final ObservableList<Queue> queueData = FXCollections.observableArrayList();
    private final ObservableList<Branch> branchData = FXCollections.observableArrayList();

    public DoctorQueueManagementController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.queueService = services.getQueueService();
        this.branchService = services.getBranchService();
        this.patientService = services.getPatientService();
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilters();
        loadBranches();
        loadQueues();
        
        // Disable action buttons until selection
        callNextButton.setDisable(true);
        markServedButton.setDisable(true);
        markNoShowButton.setDisable(true);
        
        queueTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            callNextButton.setDisable(!hasSelection || newSelection.getStatus() != QueueStatus.WAITING);
            markServedButton.setDisable(!hasSelection || newSelection.getStatus() != QueueStatus.CALLED);
            markNoShowButton.setDisable(!hasSelection || newSelection.getStatus() == QueueStatus.SERVED);
        });
        
        todayOnlyCheckBox.setSelected(true);
    }
    
    private void setupTable() {
        queueIdColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQueueId()).asObject());
        
        queueNumberColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQueueNumber()).asObject());
        
        patientNameColumn.setCellValueFactory(cellData -> {
            int patientId = cellData.getValue().getPatientId();
            try {
                Optional<Patient> patient = patientService.getPatientById(patientId);
                if (patient.isPresent()) {
                    String name = patient.get().getFirstName() + " " + patient.get().getLastName();
                    return new SimpleStringProperty(name);
                }
            } catch (Exception e) {
                // Fallback to ID
            }
            return new SimpleStringProperty("Patient #" + patientId);
        });
        
        branchColumn.setCellValueFactory(cellData -> {
            int branchId = cellData.getValue().getBranchId();
            try {
                Optional<Branch> branch = branchService.getBranchById(branchId);
                if (branch.isPresent()) {
                    return new SimpleStringProperty(branch.get().getName());
                }
            } catch (Exception e) {
                // Fallback to ID
            }
            return new SimpleStringProperty("Branch #" + branchId);
        });
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().toString()));
        
        createdAtColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(DateTimeUtil.format(cellData.getValue().getCreatedAt())));
        
        queueTable.setItems(queueData);
    }
    
    private void setupFilters() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(QueueStatus.values()));
        statusFilterComboBox.setPromptText("All Statuses");
        
        branchFilterComboBox.setPromptText("All Branches");
        branchFilterComboBox.setItems(branchData);
        
        // Add listeners for filter changes
        branchFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadQueues());
        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadQueues());
        todayOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> loadQueues());
    }
    
    private void loadBranches() {
        Task<List<Branch>> loadTask = new Task<>() {
            @Override
            protected List<Branch> call() throws Exception {
                return branchService.listBranches();
            }
            
            @Override
            protected void succeeded() {
                branchData.clear();
                branchData.addAll(getValue());
            }
            
            @Override
            protected void failed() {
                UiUtils.showError("Failed to load branches: " + getException().getMessage());
            }
        };
        
        executorService.execute(loadTask);
    }
    
    private void loadQueues() {
        Task<List<Queue>> loadTask = new Task<>() {
            @Override
            protected List<Queue> call() throws Exception {
                List<Queue> queues;
                
                // Apply filters
                boolean todayOnly = todayOnlyCheckBox.isSelected();
                Branch selectedBranch = branchFilterComboBox.getValue();
                QueueStatus selectedStatus = statusFilterComboBox.getValue();
                
                if (todayOnly) {
                    if (selectedBranch != null) {
                        queues = queueService.listTodaysQueueByBranch(selectedBranch.getBranchId());
                    } else {
                        queues = queueService.listTodaysQueue();
                    }
                } else if (selectedBranch != null) {
                    queues = queueService.listQueuesByBranch(selectedBranch.getBranchId());
                } else {
                    queues = queueService.listQueues();
                }
                
                // Filter by status if selected
                if (selectedStatus != null) {
                    queues = queues.stream()
                        .filter(q -> q.getStatus() == selectedStatus)
                        .collect(java.util.stream.Collectors.toList());
                }
                
                return queues;
            }
            
            @Override
            protected void succeeded() {
                queueData.clear();
                queueData.addAll(getValue());
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    UiUtils.showError("Failed to load queues: " + getException().getMessage());
                });
            }
        };
        
        executorService.execute(loadTask);
    }
    
    @FXML
    private void handleCallNext() {
        Queue selected = queueTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getStatus() == QueueStatus.WAITING) {
            updateQueueStatus(selected, QueueStatus.CALLED);
        }
    }
    
    @FXML
    private void handleMarkServed() {
        Queue selected = queueTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getStatus() == QueueStatus.CALLED) {
            updateQueueStatus(selected, QueueStatus.SERVED);
        }
    }
    
    @FXML
    private void handleMarkNoShow() {
        Queue selected = queueTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateQueueStatus(selected, QueueStatus.NO_SHOW);
        }
    }
    
    private void updateQueueStatus(Queue queue, QueueStatus newStatus) {
        Task<Queue> updateTask = new Task<>() {
            @Override
            protected Queue call() throws Exception {
                return queueService.updateQueueStatus(queue.getQueueId(), newStatus);
            }
            
            @Override
            protected void succeeded() {
                UiUtils.showInfo("Queue status updated to " + newStatus);
                loadQueues();
            }
            
            @Override
            protected void failed() {
                UiUtils.showError("Failed to update queue status: " + getException().getMessage());
            }
        };
        
        executorService.execute(updateTask);
    }
    
    @FXML
    private void handleRefresh() {
        loadQueues();
    }
    
    @FXML
    private void handleBack() {
        try {
            executorService.shutdownNow();
            Stage stage = (Stage) backButton.getScene().getWindow();
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
