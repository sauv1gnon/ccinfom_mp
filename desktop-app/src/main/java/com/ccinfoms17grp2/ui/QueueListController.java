package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.*;
import com.ccinfoms17grp2.services.*;
import com.ccinfoms17grp2.utils.DateTimeUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class QueueListController implements Initializable {

    @FXML private TableView<Queue> queueTable;
    @FXML private TableColumn<Queue, Integer> queueIdColumn;
    @FXML private TableColumn<Queue, Integer> queueNumberColumn;
    @FXML private TableColumn<Queue, Integer> patientIdColumn;
    @FXML private TableColumn<Queue, Integer> branchIdColumn;
    @FXML private TableColumn<Queue, String> statusColumn;
    @FXML private TableColumn<Queue, LocalDateTime> createdAtColumn;
    @FXML private ComboBox<Branch> branchFilterComboBox;
    @FXML private ComboBox<QueueStatus> statusFilterComboBox;
    @FXML private Button createButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private final User currentUser;
    private final ServiceRegistry services;
    private final ExecutorService executorService;
    private final ObservableList<Queue> queues;
    private final ObservableList<Branch> branches;
    private List<Queue> allQueues;

    public QueueListController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.executorService = Executors.newCachedThreadPool();
        this.queues = FXCollections.observableArrayList();
        this.branches = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        queueTable.setItems(queues);
        
        branchFilterComboBox.getItems().add(null);
        branchFilterComboBox.setPromptText("All Branches");
        branchFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> handleFilter());
        
        statusFilterComboBox.getItems().add(null);
        statusFilterComboBox.getItems().addAll(QueueStatus.values());
        statusFilterComboBox.setPromptText("All Statuses");
        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> handleFilter());
        
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        queueTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
        
        loadBranches();
        loadQueues();
    }

    private void setupTableColumns() {
        queueIdColumn.setCellValueFactory(new PropertyValueFactory<>("queueId"));
        queueNumberColumn.setCellValueFactory(new PropertyValueFactory<>("queueNumber"));
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        branchIdColumn.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        statusColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : ""
            )
        );
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdAtColumn.setCellFactory(column -> new TableCell<Queue, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DateTimeUtil.format(item));
            }
        });
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
            branchFilterComboBox.getItems().addAll(branches);
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load branches", task.getException());
        });
        
        executorService.execute(task);
    }

    private void loadQueues() {
        Task<List<Queue>> task = new Task<>() {
            @Override
            protected List<Queue> call() {
                return services.getQueueService().listQueues();
            }
        };

        task.setOnSucceeded(event -> {
            allQueues = task.getValue();
            handleFilter();
        });

        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load queues", task.getException());
        });

        executorService.execute(task);
    }

    private void handleFilter() {
        if (allQueues == null) {
            return;
        }
        
        Branch selectedBranch = branchFilterComboBox.getValue();
        QueueStatus selectedStatus = statusFilterComboBox.getValue();
        
        List<Queue> filtered = allQueues.stream()
            .filter(q -> selectedBranch == null || q.getBranchId() == selectedBranch.getBranchId())
            .filter(q -> selectedStatus == null || q.getStatus() == selectedStatus)
            .collect(Collectors.toList());
        
        queues.clear();
        queues.addAll(filtered);
    }

    @FXML
    private void handleCreate() {
        navigateTo(QueueFormController.class, "/com/ccinfoms17grp2/ui/queue-form.fxml", 
            () -> new QueueFormController(services, currentUser, null));
    }

    @FXML
    private void handleEdit() {
        Queue selected = queueTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            navigateTo(QueueFormController.class, "/com/ccinfoms17grp2/ui/queue-form.fxml",
                () -> new QueueFormController(services, currentUser, selected));
        }
    }

    @FXML
    private void handleDelete() {
        Queue selected = queueTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        
        boolean confirmed = UiUtils.showConfirmation(
            "Delete Queue Entry",
            "Are you sure you want to delete queue #" + selected.getQueueNumber() + "?"
        );
        
        if (!confirmed) {
            return;
        }
        
        Task<Void> deleteTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                services.getQueueService().deleteQueue(selected.getQueueId());
                return null;
            }
        };
        
        deleteTask.setOnSucceeded(event -> {
            UiUtils.showInformation("Success", "Queue entry deleted successfully");
            loadQueues();
        });
        
        deleteTask.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to delete queue entry", deleteTask.getException());
        });
        
        executorService.execute(deleteTask);
    }

    @FXML
    private void handleRefresh() {
        loadQueues();
    }

    @FXML
    private void handleBack() {
        navigateTo(AdminDashboardController.class, "/com/ccinfoms17grp2/ui/admin-dashboard.fxml",
            () -> new AdminDashboardController(services, currentUser));
    }

    private <T> void navigateTo(Class<T> controllerClass, String fxmlPath, ControllerSupplier<T> supplier) {
        Stage stage = (Stage) queueTable.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(cls -> cls == controllerClass ? supplier.get() : null);
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();
            cleanup();
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate", ex);
        }
    }

    @FunctionalInterface
    private interface ControllerSupplier<T> {
        T get();
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}