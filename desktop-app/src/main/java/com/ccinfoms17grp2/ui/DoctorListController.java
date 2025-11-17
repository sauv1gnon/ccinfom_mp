package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.DoctorAvailabilityStatus;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.DoctorService;
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
import java.util.stream.Collectors;

public class DoctorListController implements Initializable {

    @FXML
    private TableView<Doctor> doctorTable;
    @FXML
    private TableColumn<Doctor, Integer> doctorIdColumn;
    @FXML
    private TableColumn<Doctor, String> lastNameColumn;
    @FXML
    private TableColumn<Doctor, String> firstNameColumn;
    @FXML
    private TableColumn<Doctor, String> emailColumn;
    @FXML
    private TableColumn<Doctor, String> specializationsColumn;
    @FXML
    private TableColumn<Doctor, String> statusColumn;
    @FXML
    private TableColumn<Doctor, LocalDateTime> createdAtColumn;
    @FXML
    private ComboBox<DoctorAvailabilityStatus> statusFilterComboBox;
    @FXML
    private TextField searchField;
    @FXML
    private Button createButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button manageBranchesButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button backButton;

    private final ServiceRegistry services;
    private final User currentUser;
    private final ExecutorService executorService;
    private final ObservableList<Doctor> doctors;

    public DoctorListController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.executorService = Executors.newCachedThreadPool();
        this.doctors = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        doctorTable.setItems(doctors);
        
        statusFilterComboBox.getItems().add(null); // All statuses
        statusFilterComboBox.getItems().addAll(DoctorAvailabilityStatus.values());
        statusFilterComboBox.setPromptText("All Statuses");
        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> handleFilter());
        
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        manageBranchesButton.setDisable(true);
        doctorTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            manageBranchesButton.setDisable(!hasSelection);
        });
        
        loadData();
    }

    private void setupTableColumns() {
        doctorIdColumn.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        specializationsColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> {
                List<Integer> ids = cellData.getValue().getSpecializationIds();
                return ids != null ? ids.stream().map(String::valueOf).collect(Collectors.joining(", ")) : "";
            })
        );
        statusColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue().getAvailabilityStatus() != null ? 
                cellData.getValue().getAvailabilityStatus().name() : ""
            )
        );
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdAtColumn.setCellFactory(column -> new TableCell<Doctor, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DateTimeUtil.formatDateTime(item));
            }
        });
    }

    private void loadData() {
        Task<List<Doctor>> task = new Task<>() {
            @Override
            protected List<Doctor> call() {
                return services.getDoctorService().listDoctors();
            }
        };
        
        task.setOnSucceeded(event -> {
            doctors.clear();
            doctors.addAll(task.getValue());
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load doctors", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleFilter() {
        DoctorAvailabilityStatus selectedStatus = statusFilterComboBox.getValue();
        
        Task<List<Doctor>> task = new Task<>() {
            @Override
            protected List<Doctor> call() {
                if (selectedStatus == null) {
                    return services.getDoctorService().listDoctors();
                } else {
                    return services.getDoctorService().findDoctorsByAvailability(selectedStatus);
                }
            }
        };
        
        task.setOnSucceeded(event -> {
            doctors.clear();
            doctors.addAll(task.getValue());
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to filter doctors", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            loadData();
            return;
        }
        
        Task<List<Doctor>> task = new Task<>() {
            @Override
            protected List<Doctor> call() {
                return services.getDoctorService().searchDoctors(keyword.trim());
            }
        };
        
        task.setOnSucceeded(event -> {
            doctors.clear();
            doctors.addAll(task.getValue());
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to search doctors", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleCreate() {
        openDoctorForm(null);
    }

    @FXML
    private void handleEdit() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openDoctorForm(selected);
        }
    }

    @FXML
    private void handleDelete() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        boolean confirmed = UiUtils.showConfirmation(
            "Delete Doctor",
            "Are you sure you want to delete Dr. " + selected.getFirstName() + " " + selected.getLastName() + "?"
        );
        
        if (!confirmed) return;
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                services.getDoctorService().deleteDoctor(selected.getDoctorId());
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            UiUtils.showInfo("Success", "Doctor deleted successfully");
            loadData();
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to delete doctor", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleManageBranches() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openBranchAssignment(selected);
        }
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
    private void openDoctorForm(Doctor doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/doctor-form.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == DoctorFormController.class) {
                    return new DoctorFormController(services, currentUser, doctor);
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
            stage.setTitle(doctor == null ? "Create Doctor" : "Edit Doctor");
            stage.setResizable(false);
            stage.showAndWait();
            
            loadData();
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to open doctor form", ex);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    private void openBranchAssignment(Doctor doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/doctor-branch-assignment.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == DoctorBranchAssignmentController.class) {
                    return new DoctorBranchAssignmentController(services, currentUser, doctor);
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
            stage.setTitle("Manage Branch Assignments - Dr. " + doctor.getFirstName() + " " + doctor.getLastName());
            stage.setResizable(false);
            stage.showAndWait();
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to open branch assignment", ex);
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
