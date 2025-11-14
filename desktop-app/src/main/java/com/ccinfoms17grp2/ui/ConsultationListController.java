package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.*;
import com.ccinfoms17grp2.services.*;
import com.ccinfoms17grp2.utils.DateTimeUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsultationListController implements Initializable {

    @FXML private TableView<Consultation> consultationTable;
    @FXML private TableColumn<Consultation, String> consultationIdColumn;
    @FXML private TableColumn<Consultation, String> appointmentIdColumn;
    @FXML private TableColumn<Consultation, String> startTimeColumn;
    @FXML private TableColumn<Consultation, String> endTimeColumn;
    @FXML private TableColumn<Consultation, String> diagnosisColumn;

    private final User currentUser;
    private final ServiceRegistry services;
    private final ExecutorService executorService;
    private final ObservableList<Consultation> consultations = FXCollections.observableArrayList();

    public ConsultationListController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        consultationTable.setItems(consultations);
        consultationTable.setOnMouseClicked(this::handleTableClick);
        loadConsultations();
    }

    private void setupTableColumns() {
        consultationIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getConsultationId())));
        
        appointmentIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getAppointmentId())));
        
        startTimeColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(DateTimeUtil.format(data.getValue().getStartTime())));
        
        endTimeColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(DateTimeUtil.format(data.getValue().getEndTime())));
        
        diagnosisColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDiagnosis() != null ? 
                data.getValue().getDiagnosis() : "N/A"));
    }

    private void loadConsultations() {
        Task<List<Consultation>> loadTask = new Task<>() {
            @Override
            protected List<Consultation> call() {
                return new ArrayList<>();
            }
        };

        loadTask.setOnSucceeded(event -> {
            consultations.clear();
            consultations.addAll(loadTask.getValue());
        });

        loadTask.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load consultations", event.getSource().getException());
        });

        executorService.execute(loadTask);
    }

    private void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 2 && consultationTable.getSelectionModel().getSelectedItem() != null) {
            Consultation selectedConsultation = consultationTable.getSelectionModel().getSelectedItem();
            navigateToConsultationDetails(selectedConsultation);
        }
    }

    @FXML
    private void handleBackToHomepage() {
        navigateToHomepage();
    }

    @FXML
    private void handleViewAppointments() {
        navigateToAppointmentList();
    }

    @FXML
    private void handleCreateConsultation() {
        navigateToCreateConsultation();
    }

    private void navigateToHomepage() {
        Stage stage = (Stage) consultationTable.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/homepage.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == HomepageController.class) {
                    return new HomepageController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to homepage", ex);
        }
    }

    private void navigateToAppointmentList() {
        Stage stage = (Stage) consultationTable.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/appointment-list.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AppointmentListController.class) {
                    return new AppointmentListController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to appointment list", ex);
        }
    }

    private void navigateToConsultationDetails(Consultation consultation) {
        Stage stage = (Stage) consultationTable.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/consultation-details.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == ConsultationDetailsController.class) {
                    return new ConsultationDetailsController(services, currentUser, consultation);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to consultation details", ex);
        }
    }

    private void navigateToCreateConsultation() {
        Stage stage = (Stage) consultationTable.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/create-consultation.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == ConsultationCreateController.class) {
                    return new ConsultationCreateController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to create consultation", ex);
        }
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}