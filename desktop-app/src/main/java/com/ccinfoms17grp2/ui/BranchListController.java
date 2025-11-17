package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.BranchService;
import com.ccinfoms17grp2.services.ServiceRegistry;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BranchListController implements Initializable {

    @FXML
    private TableView<Branch> branchTable;
    @FXML
    private TableColumn<Branch, Integer> branchIdColumn;
    @FXML
    private TableColumn<Branch, String> branchNameColumn;
    @FXML
    private TableColumn<Branch, String> addressColumn;
    @FXML
    private TableColumn<Branch, Integer> capacityColumn;
    @FXML
    private TableColumn<Branch, String> contactColumn;
    @FXML
    private Button createButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button backButton;

    private final ServiceRegistry services;
    private final User currentUser;
    private final ExecutorService executorService;
    private final ObservableList<Branch> branches;

    public BranchListController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.executorService = Executors.newCachedThreadPool();
        this.branches = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        branchTable.setItems(branches);
        
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        branchTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
        
        loadData();
    }

    private void setupTableColumns() {
        branchIdColumn.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        branchNameColumn.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
    }

    private void loadData() {
        Task<List<Branch>> task = new Task<>() {
            @Override
            protected List<Branch> call() {
                return services.getBranchService().listBranches();
            }
        };
        
        task.setOnSucceeded(event -> {
            branches.clear();
            branches.addAll(task.getValue());
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load branches", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleCreate() {
        openBranchForm(null);
    }

    @FXML
    private void handleEdit() {
        Branch selected = branchTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openBranchForm(selected);
        }
    }

    @FXML
    private void handleDelete() {
        Branch selected = branchTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        boolean confirmed = UiUtils.showConfirmation(
            "Delete Branch",
            "Are you sure you want to delete branch: " + selected.getBranchName() + "?"
        );
        
        if (!confirmed) return;
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                services.getBranchService().deleteBranch(selected.getBranchId());
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            UiUtils.showInfo("Success", "Branch deleted successfully");
            loadData();
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to delete branch", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleBack() {
        navigateToAdminDashboard();
    }

    @SuppressWarnings("UseSpecificCatch")
    private void openBranchForm(Branch branch) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/branch-form.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == BranchFormController.class) {
                    return new BranchFormController(services, currentUser, branch);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/ccinfoms17grp2/ui/app.css").toExternalForm());
            
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(branch == null ? "Create Branch" : "Edit Branch");
            stage.setResizable(false);
            stage.showAndWait();
            
            loadData();
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to open branch form", ex);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    private void navigateToAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/admin-dashboard.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AdminDashboardController.class) {
                    return new AdminDashboardController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/ccinfoms17grp2/ui/app.css").toExternalForm());
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Portal");
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to admin dashboard", ex);
        }
    }
}
