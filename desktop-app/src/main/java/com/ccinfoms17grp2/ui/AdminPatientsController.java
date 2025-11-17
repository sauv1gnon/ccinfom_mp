package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.ValidationException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AdminPatientsController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;

    @FXML
    private TableView<Patient> patientsTable;

    @FXML
    private TableColumn<Patient, Integer> patientIdColumn;

    @FXML
    private TableColumn<Patient, String> firstNameColumn;

    @FXML
    private TableColumn<Patient, String> lastNameColumn;

    @FXML
    private TableColumn<Patient, String> emailColumn;

    @FXML
    private TableColumn<Patient, String> contactColumn;

    @FXML
    private TableColumn<Patient, LocalDateTime> registeredAtColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        registeredAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        registeredAtColumn.setCellFactory(column -> new TableCell<Patient, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    @FXML
    private void handleAddPatient() {
        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle("Add Patient");
        dialog.setHeaderText("Register a new patient");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField contactField = new TextField();
        contactField.setPromptText("Contact Number");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Contact:"), 0, 3);
        grid.add(contactField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Patient patient = new Patient();
                patient.setFirstName(firstNameField.getText() == null ? "" : firstNameField.getText().trim());
                patient.setLastName(lastNameField.getText() == null ? "" : lastNameField.getText().trim());
                patient.setEmail(emailField.getText() == null ? "" : emailField.getText().trim());
                patient.setContactNumber(contactField.getText() == null ? "" : contactField.getText().trim());
                return patient;
            }
            return null;
        });

        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(patient -> {
            if (patient.getFirstName().isEmpty() || patient.getLastName().isEmpty() || 
                patient.getContactNumber().isEmpty()) {
                statusLabel.setText("First name, last name, and contact are required");
                return;
            }
            try {
                Patient created = services.getPatientService().createPatient(patient);
                statusLabel.setText("Patient created successfully: " + created.getFirstName() + " " + created.getLastName());
                loadPatients();
            } catch (ValidationException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to create patient");
                UiUtils.showError("Create Patient Failed", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleEditPatient() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a patient to edit");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle("Edit Patient");
        dialog.setHeaderText("Edit patient: " + selected.getFirstName() + " " + selected.getLastName());

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        TextField firstNameField = new TextField(selected.getFirstName());
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField(selected.getLastName());
        lastNameField.setPromptText("Last Name");
        TextField emailField = new TextField(selected.getEmail());
        emailField.setPromptText("Email");
        TextField contactField = new TextField(selected.getContactNumber());
        contactField.setPromptText("Contact Number");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Contact:"), 0, 3);
        grid.add(contactField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                Patient patient = new Patient();
                patient.setPatientId(selected.getPatientId());
                patient.setFirstName(firstNameField.getText() == null ? "" : firstNameField.getText().trim());
                patient.setLastName(lastNameField.getText() == null ? "" : lastNameField.getText().trim());
                patient.setEmail(emailField.getText() == null ? "" : emailField.getText().trim());
                patient.setContactNumber(contactField.getText() == null ? "" : contactField.getText().trim());
                return patient;
            }
            return null;
        });

        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(patient -> {
            if (patient.getFirstName().isEmpty() || patient.getLastName().isEmpty() || 
                patient.getEmail().isEmpty() || patient.getContactNumber().isEmpty()) {
                statusLabel.setText("All fields are required");
                return;
            }
            try {
                services.getPatientService().updatePatient(patient);
                statusLabel.setText("Patient updated successfully");
                loadPatients();
            } catch (ValidationException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to update patient");
                UiUtils.showError("Update Patient Failed", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleDeletePatient() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a patient to delete");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        boolean confirm = UiUtils.showConfirmation(
            "Delete Patient",
            "Are you sure you want to delete patient: " + selected.getFirstName() + " " + selected.getLastName() + "?"
        );

        if (confirm) {
            try {
                services.getPatientService().deletePatient(selected.getPatientId());
                statusLabel.setText("Patient deleted successfully");
                loadPatients();
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to delete patient");
                UiUtils.showError("Delete Patient Failed", ex.getMessage());
            }
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
            loadPatients();
            return;
        }

        try {
            List<Patient> allPatients = services.getPatientService().listPatients();
            List<Patient> filtered = allPatients.stream()
                .filter(p -> 
                    (p.getFirstName() != null && p.getFirstName().toLowerCase().contains(query.toLowerCase())) ||
                    (p.getLastName() != null && p.getLastName().toLowerCase().contains(query.toLowerCase())) ||
                    (p.getEmail() != null && p.getEmail().toLowerCase().contains(query.toLowerCase()))
                )
                .collect(java.util.stream.Collectors.toList());
            patientsTable.setItems(FXCollections.observableArrayList(filtered));
            statusLabel.setText("Found " + filtered.size() + " patients");
        } catch (RuntimeException ex) {
            statusLabel.setText("Search failed");
            UiUtils.showError("Search Failed", ex.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadPatients();
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
        loadPatients();
    }

    private void loadPatients() {
        if (services == null || patientsTable == null) {
            return;
        }

        try {
            List<Patient> patients = services.getPatientService().listPatients();
            patientsTable.setItems(FXCollections.observableArrayList(patients));
            statusLabel.setText("Loaded " + patients.size() + " patients");
        } catch (RuntimeException ex) {
            statusLabel.setText("Failed to load patients");
            UiUtils.showError("Load Patients Failed", ex.getMessage());
        }
    }
}
