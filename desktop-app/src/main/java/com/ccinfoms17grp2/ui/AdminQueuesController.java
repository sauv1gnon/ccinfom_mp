package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Queue;
import com.ccinfoms17grp2.models.QueueStatus;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;

public class AdminQueuesController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;

    @FXML
    private TableView<Queue> queuesTable;

    @FXML
    private TableColumn<Queue, Integer> queueIdColumn;

    @FXML
    private TableColumn<Queue, Integer> branchIdColumn;

    @FXML
    private TableColumn<Queue, LocalDateTime> queueDateColumn;

    @FXML
    private TableColumn<Queue, QueueStatus> statusColumn;

    @FXML
    private TableColumn<Queue, Integer> currentPositionColumn;

    @FXML
    private TableColumn<Queue, Integer> totalPositionsColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        queueIdColumn.setCellValueFactory(new PropertyValueFactory<>("queueId"));
        branchIdColumn.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        queueDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        currentPositionColumn.setCellValueFactory(new PropertyValueFactory<>("queueNumber"));

        queueDateColumn.setCellFactory(column -> new TableCell<Queue, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalDate().toString());
                }
            }
        });

        totalPositionsColumn.setCellValueFactory(cellData -> {
            Queue queue = cellData.getValue();
            if (services != null) {
                try {
                    int count = services.getQueueService().listTodaysQueueByBranch(queue.getBranchId()).size();
                    return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
                }
            }
            return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
        });
    }

    @FXML
    private void handleAdvanceQueue() {
        Queue selected = queuesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a queue to advance");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        try {
            services.getQueueService().updateQueueStatus(selected.getQueueId(), QueueStatus.CALLED);
            statusLabel.setText("Queue advanced for queue #" + selected.getQueueId());
            loadQueues();
        } catch (RuntimeException ex) {
            statusLabel.setText("Failed to advance queue");
            UiUtils.showError("Advance Queue Failed", ex.getMessage());
        }
    }

    @FXML
    private void handleViewPositions() {
        Queue selected = queuesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a queue to view positions");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        try {
            List<Queue> queues = services.getQueueService().listTodaysQueueByBranch(selected.getBranchId());
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Queue Positions");
            alert.setHeaderText("Positions for Branch #" + selected.getBranchId());
            
            StringBuilder details = new StringBuilder();
            if (queues.isEmpty()) {
                details.append("No positions in queue");
            } else {
                for (Queue q : queues) {
                    details.append("Queue #").append(q.getQueueNumber())
                           .append(": Patient #").append(q.getPatientId())
                           .append(" (").append(q.getStatus()).append(")\n");
                }
            }
            
            alert.setContentText(details.toString());
            alert.showAndWait();
        } catch (RuntimeException ex) {
            statusLabel.setText("Failed to load queue positions");
            UiUtils.showError("Load Positions Failed", ex.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        if (query.isEmpty()) {
            loadQueues();
            return;
        }

        try {
            int queueId = Integer.parseInt(query);
            java.util.Optional<Queue> queueOpt = services.getQueueService().getQueueById(queueId);
            if (queueOpt.isPresent()) {
                queuesTable.setItems(FXCollections.observableArrayList(queueOpt.get()));
                statusLabel.setText("Found queue #" + queueId);
            } else {
                queuesTable.setItems(FXCollections.observableArrayList());
                statusLabel.setText("No queue found with ID: " + queueId);
            }
        } catch (NumberFormatException ex) {
            statusLabel.setText("Invalid queue ID");
        } catch (RuntimeException ex) {
            statusLabel.setText("Search failed");
            UiUtils.showError("Search Failed", ex.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadQueues();
    }

    @FXML
    private void handleBackToDashboard() {
        navigator.show(UiView.ADMIN_DASHBOARD);
    }

    @Override
    public void setNavigator(SceneNavigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void setServices(ServiceRegistry services) {
        this.services = services;
    }

    @Override
    public void onDisplay() {
        statusLabel.setText("");
        loadQueues();
    }

    private void loadQueues() {
        if (services == null || queuesTable == null) {
            return;
        }

        try {
            List<Queue> queues = services.getQueueService().listTodaysQueue();
            queuesTable.setItems(FXCollections.observableArrayList(queues));
            statusLabel.setText("Loaded " + queues.size() + " queues for today");
        } catch (RuntimeException ex) {
            statusLabel.setText("Failed to load queues");
            UiUtils.showError("Load Queues Failed", ex.getMessage());
        }
    }
}
