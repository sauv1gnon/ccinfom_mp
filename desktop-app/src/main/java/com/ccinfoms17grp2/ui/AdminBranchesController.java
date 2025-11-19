package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.ValidationException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class AdminBranchesController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;

    @FXML
    private TableView<Branch> branchesTable;

    @FXML
    private TableColumn<Branch, Integer> branchIdColumn;

    @FXML
    private TableColumn<Branch, String> branchNameColumn;

    @FXML
    private TableColumn<Branch, String> addressColumn;

    @FXML
    private TableColumn<Branch, Integer> capacityColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        branchIdColumn.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        branchNameColumn.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
    }

    @FXML
    private void handleAddBranch() {
        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-branch-form.fxml"));
            javafx.scene.Parent root = loader.load();
            
            AdminBranchFormController controller = loader.getController();
            controller.setServices(services);
            controller.setCreateMode();
            
            Stage stage = new Stage();
            stage.setTitle("Add Branch");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            Branch result = controller.getResultBranch();
            if (result != null) {
                try {
                    Branch created = services.getBranchService().createBranch(result);
                    statusLabel.setText("Branch created: " + created.getBranchName());
                    loadBranches();
                } catch (ValidationException ex) {
                    statusLabel.setText(ex.getMessage());
                } catch (RuntimeException ex) {
                    statusLabel.setText("Failed to create branch");
                    UiUtils.showError("Create Branch Failed", ex.getMessage());
                }
            }
        } catch (Exception ex) {
            statusLabel.setText("Failed to open form");
            UiUtils.showError("Form Error", ex.getMessage());
        }
    }

    @FXML
    private void handleEditBranch() {
        Branch selected = branchesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a branch to edit");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-branch-form.fxml"));
            javafx.scene.Parent root = loader.load();
            
            AdminBranchFormController controller = loader.getController();
            controller.setServices(services);
            controller.setExistingBranch(selected);
            
            Stage stage = new Stage();
            stage.setTitle("Edit Branch");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            Branch result = controller.getResultBranch();
            if (result != null) {
                try {
                    services.getBranchService().updateBranch(result);
                    statusLabel.setText("Branch updated successfully");
                    loadBranches();
                } catch (ValidationException ex) {
                    statusLabel.setText(ex.getMessage());
                } catch (RuntimeException ex) {
                    statusLabel.setText("Failed to update branch");
                    UiUtils.showError("Update Branch Failed", ex.getMessage());
                }
            }
        } catch (Exception ex) {
            statusLabel.setText("Failed to open form");
            UiUtils.showError("Form Error", ex.getMessage());
        }
    }

    @FXML
    private void handleDeleteBranch() {
        Branch selected = branchesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a branch to delete");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        boolean confirm = UiUtils.showConfirmation(
            "Delete Branch",
            "Are you sure you want to delete: " + selected.getBranchName() + "?"
        );

        if (confirm) {
            try {
                services.getBranchService().deleteBranch(selected.getBranchId());
                statusLabel.setText("Branch deleted successfully");
                loadBranches();
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to delete branch");
                UiUtils.showError("Delete Branch Failed", ex.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadBranches();
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
        loadBranches();
    }

    private void loadBranches() {
        if (services == null || branchesTable == null) {
            return;
        }

        try {
            List<Branch> branches = services.getBranchService().listBranches();
            branchesTable.setItems(FXCollections.observableArrayList(branches));
            statusLabel.setText("Loaded " + branches.size() + " branches");
        } catch (RuntimeException ex) {
            statusLabel.setText("Failed to load branches");
            UiUtils.showError("Load Branches Failed", ex.getMessage());
        }
    }
}
