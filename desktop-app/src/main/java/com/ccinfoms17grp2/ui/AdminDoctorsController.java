package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.DoctorAvailabilityStatus;
import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.Specialization;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.scene.layout.GridPane;
import com.ccinfoms17grp2.services.ValidationException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminDoctorsController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;

    @FXML
    private TableView<Doctor> doctorsTable;

    @FXML
    private TableColumn<Doctor, Integer> doctorIdColumn;

    @FXML
    private TableColumn<Doctor, String> firstNameColumn;

    @FXML
    private TableColumn<Doctor, String> lastNameColumn;

    @FXML
    private TableColumn<Doctor, String> specializationColumn;

    @FXML
    private TableColumn<Doctor, String> licenseNumberColumn;

    @FXML
    private TableColumn<Doctor, DoctorAvailabilityStatus> availabilityColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        doctorIdColumn.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        availabilityColumn.setCellValueFactory(new PropertyValueFactory<>("availabilityStatus"));

        specializationColumn.setCellValueFactory(cellData -> {
            Doctor doctor = cellData.getValue();
            if (doctor.getSpecializationIds() != null && !doctor.getSpecializationIds().isEmpty()) {
                String specIds = doctor.getSpecializationIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
                return new javafx.beans.property.SimpleStringProperty(specIds);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        licenseNumberColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    @FXML
    private void handleAddDoctor() {
        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        Dialog<Doctor> dialog = new Dialog<>();
        dialog.setTitle("Add Doctor");
        dialog.setHeaderText("Register a new doctor");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        List<Specialization> allSpecs = services.getSpecializationService().listSpecializations();
        ListView<Specialization> specListView = new ListView<>(FXCollections.observableArrayList(allSpecs));
        specListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        specListView.setPrefHeight(100);
        specListView.setCellFactory(param -> new ListCell<Specialization>() {
            @Override
            protected void updateItem(Specialization item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getSpecializationName());
            }
        });

        ChoiceBox<DoctorAvailabilityStatus> statusChoice = new ChoiceBox<>(FXCollections.observableArrayList(DoctorAvailabilityStatus.values()));
        statusChoice.setValue(DoctorAvailabilityStatus.AVAILABLE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Specializations:"), 0, 3);
        grid.add(specListView, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusChoice, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Doctor doctor = new Doctor();
                doctor.setFirstName(firstNameField.getText() == null ? "" : firstNameField.getText().trim());
                doctor.setLastName(lastNameField.getText() == null ? "" : lastNameField.getText().trim());
                doctor.setEmail(emailField.getText() == null ? "" : emailField.getText().trim());
                doctor.setAvailabilityStatus(statusChoice.getValue());
                List<Integer> specIds = specListView.getSelectionModel().getSelectedItems().stream()
                    .map(Specialization::getSpecializationId)
                    .collect(Collectors.toList());
                doctor.setSpecializationIds(specIds);
                return doctor;
            }
            return null;
        });

        Optional<Doctor> result = dialog.showAndWait();
        result.ifPresent(doctor -> {
            if (doctor.getFirstName().isEmpty() || doctor.getLastName().isEmpty()) {
                statusLabel.setText("First and last names are required");
                return;
            }
            try {
                Doctor created = services.getDoctorService().createDoctor(doctor);
                statusLabel.setText("Doctor created successfully: " + created.getFirstName() + " " + created.getLastName());
                loadDoctors();
            } catch (ValidationException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to create doctor");
                UiUtils.showError("Create Doctor Failed", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleEditDoctor() {
        Doctor selected = doctorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a doctor to edit");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        Dialog<Doctor> dialog = new Dialog<>();
        dialog.setTitle("Edit Doctor");
        dialog.setHeaderText("Edit doctor: " + selected.getFirstName() + " " + selected.getLastName());

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        TextField firstNameField = new TextField(selected.getFirstName());
        TextField lastNameField = new TextField(selected.getLastName());
        TextField emailField = new TextField(selected.getEmail() != null ? selected.getEmail() : "");

        List<Specialization> allSpecs = services.getSpecializationService().listSpecializations();
        ListView<Specialization> specListView = new ListView<>(FXCollections.observableArrayList(allSpecs));
        specListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        specListView.setPrefHeight(100);
        specListView.setCellFactory(param -> new ListCell<Specialization>() {
            @Override
            protected void updateItem(Specialization item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getSpecializationName());
            }
        });
        for (Integer specId : selected.getSpecializationIds()) {
            for (int i = 0; i < allSpecs.size(); i++) {
                if (allSpecs.get(i).getSpecializationId() == specId) {
                    specListView.getSelectionModel().select(i);
                }
            }
        }

        ChoiceBox<DoctorAvailabilityStatus> statusChoice = new ChoiceBox<>(FXCollections.observableArrayList(DoctorAvailabilityStatus.values()));
        statusChoice.setValue(selected.getAvailabilityStatus());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Specializations:"), 0, 3);
        grid.add(specListView, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusChoice, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(selected.getDoctorId());
                doctor.setFirstName(firstNameField.getText() == null ? "" : firstNameField.getText().trim());
                doctor.setLastName(lastNameField.getText() == null ? "" : lastNameField.getText().trim());
                doctor.setEmail(emailField.getText() == null ? "" : emailField.getText().trim());
                doctor.setAvailabilityStatus(statusChoice.getValue());
                List<Integer> specIds = specListView.getSelectionModel().getSelectedItems().stream()
                    .map(Specialization::getSpecializationId)
                    .collect(Collectors.toList());
                doctor.setSpecializationIds(specIds);
                return doctor;
            }
            return null;
        });

        Optional<Doctor> result = dialog.showAndWait();
        result.ifPresent(doctor -> {
            if (doctor.getFirstName().isEmpty() || doctor.getLastName().isEmpty()) {
                statusLabel.setText("First and last names are required");
                return;
            }
            try {
                services.getDoctorService().updateDoctor(doctor);
                statusLabel.setText("Doctor updated successfully");
                loadDoctors();
            } catch (ValidationException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to update doctor");
                UiUtils.showError("Update Doctor Failed", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteDoctor() {
        Doctor selected = doctorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a doctor to delete");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        boolean confirm = UiUtils.showConfirmation(
            "Delete Doctor",
            "Are you sure you want to delete doctor: " + selected.getFirstName() + " " + selected.getLastName() + "?"
        );

        if (confirm) {
            try {
                services.getDoctorService().deleteDoctor(selected.getDoctorId());
                statusLabel.setText("Doctor deleted successfully");
                loadDoctors();
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to delete doctor");
                UiUtils.showError("Delete Doctor Failed", ex.getMessage());
            }
        }
    }

    @FXML
    private void handleAssignToBranch() {
        Doctor selected = doctorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a doctor to assign");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        List<Branch> branches = services.getBranchService().listBranches();
        ChoiceDialog<Branch> dialog = new ChoiceDialog<>(branches.isEmpty() ? null : branches.get(0), branches);
        dialog.setTitle("Assign Doctor to Branch");
        dialog.setHeaderText("Select a branch for " + selected.getFirstName() + " " + selected.getLastName());
        dialog.setContentText("Branch:");

        Optional<Branch> result = dialog.showAndWait();
        result.ifPresent(branch -> {
            try {
                services.getDoctorService().assignDoctorToBranches(selected.getDoctorId(), java.util.Arrays.asList(branch.getBranchId()));
                statusLabel.setText("Doctor assigned to " + branch.getBranchName());
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to assign doctor");
                UiUtils.showError("Assign Doctor Failed", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleSearch() {
        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        if (query.isEmpty()) {
            loadDoctors();
            return;
        }

        try {
            List<Doctor> allDoctors = services.getDoctorService().listDoctors();
            List<Doctor> filtered = allDoctors.stream()
                .filter(d -> 
                    (d.getFirstName() != null && d.getFirstName().toLowerCase().contains(query.toLowerCase())) ||
                    (d.getLastName() != null && d.getLastName().toLowerCase().contains(query.toLowerCase()))
                )
                .collect(java.util.stream.Collectors.toList());
            doctorsTable.setItems(FXCollections.observableArrayList(filtered));
            statusLabel.setText("Found " + filtered.size() + " doctors");
        } catch (RuntimeException ex) {
            statusLabel.setText("Search failed");
            UiUtils.showError("Search Failed", ex.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadDoctors();
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
        loadDoctors();
    }

    private void loadDoctors() {
        if (services == null || doctorsTable == null) {
            return;
        }

        try {
            List<Doctor> doctors = services.getDoctorService().listDoctors();
            doctorsTable.setItems(FXCollections.observableArrayList(doctors));
            statusLabel.setText("Loaded " + doctors.size() + " doctors");
        } catch (RuntimeException ex) {
            statusLabel.setText("Failed to load doctors");
            UiUtils.showError("Load Doctors Failed", ex.getMessage());
        }
    }
}
