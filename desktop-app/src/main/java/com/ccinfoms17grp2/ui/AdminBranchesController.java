package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.scene.layout.GridPane;
import com.ccinfoms17grp2.services.ValidationException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;

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

        Dialog<Branch> dialog = new Dialog<>();
        dialog.setTitle("Add Branch");
        dialog.setHeaderText("Create a new branch");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Branch Name");
        TextField addressField = new TextField();
        addressField.setPromptText("Address");
        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity");
        TextField contactField = new TextField();
        contactField.setPromptText("Contact Number");
        TextField latField = new TextField();
        latField.setPromptText("Latitude (optional)");
        TextField lonField = new TextField();
        lonField.setPromptText("Longitude (optional)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("Capacity:"), 0, 2);
        grid.add(capacityField, 1, 2);
        grid.add(new Label("Contact:"), 0, 3);
        grid.add(contactField, 1, 3);
        grid.add(new Label("Latitude:"), 0, 4);
        grid.add(latField, 1, 4);
        grid.add(new Label("Longitude:"), 0, 5);
        grid.add(lonField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Branch branch = new Branch();
                branch.setBranchName(nameField.getText() == null ? "" : nameField.getText().trim());
                branch.setAddress(addressField.getText() == null ? "" : addressField.getText().trim());
                branch.setContactNumber(contactField.getText() == null ? "" : contactField.getText().trim());
                try {
                    String capText = capacityField.getText() == null ? "" : capacityField.getText().trim();
                    branch.setCapacity(capText.isEmpty() ? 0 : Integer.parseInt(capText));
                } catch (NumberFormatException e) {
                    branch.setCapacity(0);
                }
                try {
                    String latText = latField.getText() == null ? "" : latField.getText().trim();
                    if (!latText.isEmpty()) {
                        branch.setLatitude(Double.parseDouble(latText));
                    }
                } catch (NumberFormatException e) {
                }
                try {
                    String lonText = lonField.getText() == null ? "" : lonField.getText().trim();
                    if (!lonText.isEmpty()) {
                        branch.setLongitude(Double.parseDouble(lonText));
                    }
                } catch (NumberFormatException e) {
                }
                return branch;
            }
            return null;
        });

        Optional<Branch> result = dialog.showAndWait();
        result.ifPresent(branch -> {
            if (branch.getBranchName().isEmpty() || branch.getAddress().isEmpty()) {
                statusLabel.setText("Name and address are required");
                return;
            }
            try {
                Branch created = services.getBranchService().createBranch(branch);
                statusLabel.setText("Branch created: " + created.getBranchName());
                loadBranches();
            } catch (ValidationException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to create branch");
                UiUtils.showError("Create Branch Failed", ex.getMessage());
            }
        });
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

        Dialog<Branch> dialog = new Dialog<>();
        dialog.setTitle("Edit Branch");
        dialog.setHeaderText("Edit branch: " + selected.getBranchName());

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(selected.getBranchName());
        TextField addressField = new TextField(selected.getAddress());
        TextField capacityField = new TextField(String.valueOf(selected.getCapacity()));
        TextField contactField = new TextField(selected.getContactNumber() != null ? selected.getContactNumber() : "");
        TextField latField = new TextField(selected.getLatitude() != null ? selected.getLatitude().toString() : "");
        TextField lonField = new TextField(selected.getLongitude() != null ? selected.getLongitude().toString() : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("Capacity:"), 0, 2);
        grid.add(capacityField, 1, 2);
        grid.add(new Label("Contact:"), 0, 3);
        grid.add(contactField, 1, 3);
        grid.add(new Label("Latitude:"), 0, 4);
        grid.add(latField, 1, 4);
        grid.add(new Label("Longitude:"), 0, 5);
        grid.add(lonField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                Branch branch = new Branch();
                branch.setBranchId(selected.getBranchId());
                branch.setBranchName(nameField.getText() == null ? "" : nameField.getText().trim());
                branch.setAddress(addressField.getText() == null ? "" : addressField.getText().trim());
                branch.setContactNumber(contactField.getText() == null ? "" : contactField.getText().trim());
                try {
                    String capText = capacityField.getText() == null ? "" : capacityField.getText().trim();
                    branch.setCapacity(capText.isEmpty() ? 0 : Integer.parseInt(capText));
                } catch (NumberFormatException e) {
                    branch.setCapacity(selected.getCapacity());
                }
                try {
                    String latText = latField.getText() == null ? "" : latField.getText().trim();
                    if (!latText.isEmpty()) {
                        branch.setLatitude(Double.parseDouble(latText));
                    }
                } catch (NumberFormatException e) {
                }
                try {
                    String lonText = lonField.getText() == null ? "" : lonField.getText().trim();
                    if (!lonText.isEmpty()) {
                        branch.setLongitude(Double.parseDouble(lonText));
                    }
                } catch (NumberFormatException e) {
                }
                return branch;
            }
            return null;
        });

        Optional<Branch> result = dialog.showAndWait();
        result.ifPresent(branch -> {
            if (branch.getBranchName().isEmpty() || branch.getAddress().isEmpty()) {
                statusLabel.setText("Name and address are required");
                return;
            }
            try {
                services.getBranchService().updateBranch(branch);
                statusLabel.setText("Branch updated successfully");
                loadBranches();
            } catch (ValidationException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to update branch");
                UiUtils.showError("Update Branch Failed", ex.getMessage());
            }
        });
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
