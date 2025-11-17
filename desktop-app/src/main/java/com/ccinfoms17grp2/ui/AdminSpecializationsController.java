package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Specialization;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.scene.layout.GridPane;
import com.ccinfoms17grp2.services.ValidationException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AdminSpecializationsController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;

    @FXML
    private TableView<Specialization> specializationsTable;

    @FXML
    private TableColumn<Specialization, Integer> specializationIdColumn;

    @FXML
    private TableColumn<Specialization, String> specializationNameColumn;

    @FXML
    private TableColumn<Specialization, String> specializationCodeColumn;

    @FXML
    private TableColumn<Specialization, LocalDateTime> createdAtColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        specializationIdColumn.setCellValueFactory(new PropertyValueFactory<>("specializationId"));
        specializationNameColumn.setCellValueFactory(new PropertyValueFactory<>("specializationName"));
        specializationCodeColumn.setCellValueFactory(new PropertyValueFactory<>("specializationCode"));
    }

    @FXML
    private void handleAddSpecialization() {
        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        Dialog<Specialization> dialog = new Dialog<>();
        dialog.setTitle("Add Specialization");
        dialog.setHeaderText("Create a new specialization");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Specialization Name");
        TextField codeField = new TextField();
        codeField.setPromptText("Specialization Code");
        TextArea descField = new TextArea();
        descField.setPromptText("Description (optional)");
        descField.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Code:"), 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Specialization spec = new Specialization();
                spec.setSpecializationName(nameField.getText() == null ? "" : nameField.getText().trim());
                spec.setSpecializationCode(codeField.getText() == null ? "" : codeField.getText().trim());
                spec.setDescription(descField.getText() == null ? "" : descField.getText().trim());
                return spec;
            }
            return null;
        });

        Optional<Specialization> result = dialog.showAndWait();
        result.ifPresent(spec -> {
            if (spec.getSpecializationName().isEmpty() || spec.getSpecializationCode().isEmpty()) {
                statusLabel.setText("Name and code are required");
                return;
            }
            try {
                Specialization created = services.getSpecializationService().createSpecialization(spec);
                statusLabel.setText("Specialization created: " + created.getSpecializationName());
                loadSpecializations();
            } catch (ValidationException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to create specialization");
                UiUtils.showError("Create Specialization Failed", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleEditSpecialization() {
        Specialization selected = specializationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a specialization to edit");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        Dialog<Specialization> dialog = new Dialog<>();
        dialog.setTitle("Edit Specialization");
        dialog.setHeaderText("Edit specialization: " + selected.getSpecializationName());

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(selected.getSpecializationName());
        TextField codeField = new TextField(selected.getSpecializationCode());
        TextArea descField = new TextArea(selected.getDescription() != null ? selected.getDescription() : "");
        descField.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Code:"), 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                Specialization spec = new Specialization();
                spec.setSpecializationId(selected.getSpecializationId());
                spec.setSpecializationName(nameField.getText() == null ? "" : nameField.getText().trim());
                spec.setSpecializationCode(codeField.getText() == null ? "" : codeField.getText().trim());
                spec.setDescription(descField.getText() == null ? "" : descField.getText().trim());
                return spec;
            }
            return null;
        });

        Optional<Specialization> result = dialog.showAndWait();
        result.ifPresent(spec -> {
            if (spec.getSpecializationName().isEmpty() || spec.getSpecializationCode().isEmpty()) {
                statusLabel.setText("Name and code are required");
                return;
            }
            try {
                services.getSpecializationService().updateSpecialization(spec);
                statusLabel.setText("Specialization updated successfully");
                loadSpecializations();
            } catch (ValidationException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to update specialization");
                UiUtils.showError("Update Specialization Failed", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteSpecialization() {
        Specialization selected = specializationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a specialization to delete");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        boolean confirm = UiUtils.showConfirmation(
            "Delete Specialization",
            "Are you sure you want to delete: " + selected.getSpecializationName() + "?"
        );

        if (confirm) {
            try {
                services.getSpecializationService().deleteSpecialization(selected.getSpecializationId());
                statusLabel.setText("Specialization deleted successfully");
                loadSpecializations();
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to delete specialization");
                UiUtils.showError("Delete Specialization Failed", ex.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadSpecializations();
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
        loadSpecializations();
    }

    private void loadSpecializations() {
        if (services == null || specializationsTable == null) {
            return;
        }

        try {
            List<Specialization> specs = services.getSpecializationService().listSpecializations();
            specializationsTable.setItems(FXCollections.observableArrayList(specs));
            statusLabel.setText("Loaded " + specs.size() + " specializations");
        } catch (RuntimeException ex) {
            statusLabel.setText("Failed to load specializations");
            UiUtils.showError("Load Specializations Failed", ex.getMessage());
        }
    }
}
