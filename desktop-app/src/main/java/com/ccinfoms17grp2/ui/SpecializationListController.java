package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Specialization;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.SpecializationService;
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

public class SpecializationListController implements Initializable {

    @FXML
    private TableView<Specialization> specializationTable;
    @FXML
    private TableColumn<Specialization, Integer> specializationIdColumn;
    @FXML
    private TableColumn<Specialization, String> nameColumn;
    @FXML
    private TableColumn<Specialization, String> codeColumn;
    @FXML
    private TableColumn<Specialization, String> descriptionColumn;
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
    private final ObservableList<Specialization> specializations;

    public SpecializationListController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.executorService = Executors.newCachedThreadPool();
        this.specializations = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        specializationTable.setItems(specializations);
        
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        specializationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
        
        loadData();
    }

    private void setupTableColumns() {
        specializationIdColumn.setCellValueFactory(new PropertyValueFactory<>("specializationId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("specializationName"));
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("specializationCode"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void loadData() {
        Task<List<Specialization>> task = new Task<>() {
            @Override
            protected List<Specialization> call() {
                return services.getSpecializationService().listSpecializations();
            }
        };
        
        task.setOnSucceeded(event -> {
            specializations.clear();
            specializations.addAll(task.getValue());
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load specializations", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleCreate() {
        openSpecializationForm(null);
    }

    @FXML
    private void handleEdit() {
        Specialization selected = specializationTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openSpecializationForm(selected);
        }
    }

    @FXML
    private void handleDelete() {
        Specialization selected = specializationTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        boolean confirmed = UiUtils.showConfirmation(
            "Delete Specialization",
            "Are you sure you want to delete specialization: " + selected.getSpecializationName() + "?"
        );
        
        if (!confirmed) return;
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                services.getSpecializationService().deleteSpecialization(selected.getSpecializationId());
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            UiUtils.showInfo("Success", "Specialization deleted successfully");
            loadData();
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to delete specialization", task.getException());
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
    private void openSpecializationForm(Specialization specialization) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/specialization-form.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == SpecializationFormController.class) {
                    return new SpecializationFormController(services, currentUser, specialization);
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
            stage.setTitle(specialization == null ? "Create Specialization" : "Edit Specialization");
            stage.setResizable(false);
            stage.showAndWait();
            
            loadData();
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to open specialization form", ex);
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
