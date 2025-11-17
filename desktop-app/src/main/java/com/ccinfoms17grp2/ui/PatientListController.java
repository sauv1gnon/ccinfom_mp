package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.PatientService;
import com.ccinfoms17grp2.services.ServiceRegistry;
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

public class PatientListController implements Initializable {

    @FXML
    private TableView<Patient> patientTable;
    @FXML
    private TableColumn<Patient, Integer> patientIdColumn;
    @FXML
    private TableColumn<Patient, String> lastNameColumn;
    @FXML
    private TableColumn<Patient, String> firstNameColumn;
    @FXML
    private TableColumn<Patient, String> contactColumn;
    @FXML
    private TableColumn<Patient, String> emailColumn;
    @FXML
    private TableColumn<Patient, LocalDateTime> createdAtColumn;
    @FXML
    private TextField searchField;
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
    private final ObservableList<Patient> patients;

    public PatientListController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.executorService = Executors.newCachedThreadPool();
        this.patients = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        patientTable.setItems(patients);
        
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
        
        loadData();
    }

    private void setupTableColumns() {
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdAtColumn.setCellFactory(column -> new TableCell<Patient, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DateTimeUtil.formatDateTime(item));
            }
        });
    }

    private void loadData() {
        Task<List<Patient>> task = new Task<>() {
            @Override
            protected List<Patient> call() {
                return services.getPatientService().listPatients();
            }
        };
        
        task.setOnSucceeded(event -> {
            patients.clear();
            patients.addAll(task.getValue());
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load patients", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        Task<List<Patient>> task = new Task<>() {
            @Override
            protected List<Patient> call() {
                return services.getPatientService().searchPatients(keyword);
            }
        };
        
        task.setOnSucceeded(event -> {
            patients.clear();
            patients.addAll(task.getValue());
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to search patients", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleCreate() {
        openPatientForm(null);
    }

    @FXML
    private void handleEdit() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openPatientForm(selected);
        }
    }

    @FXML
    private void handleDelete() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        boolean confirmed = UiUtils.showConfirmation(
            "Delete Patient",
            "Are you sure you want to delete patient: " + selected.getFirstName() + " " + selected.getLastName() + "?"
        );
        
        if (!confirmed) return;
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                services.getPatientService().deletePatient(selected.getPatientId());
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            UiUtils.showInfo("Success", "Patient deleted successfully");
            loadData();
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to delete patient", task.getException());
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
    private void openPatientForm(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/patient-form.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == PatientFormController.class) {
                    return new PatientFormController(services, currentUser, patient);
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
            stage.setTitle(patient == null ? "Create Patient" : "Edit Patient");
            stage.setResizable(false);
            stage.showAndWait();
            
            loadData();
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to open patient form", ex);
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
