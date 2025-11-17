package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.Consultation;
import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.AppointmentService;
import com.ccinfoms17grp2.services.ConsultationService;
import com.ccinfoms17grp2.services.PatientService;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.utils.DateTimeUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorConsultationManagementController implements Initializable {

    @FXML private TableView<Consultation> consultationTable;
    @FXML private TableColumn<Consultation, Integer> consultationIdColumn;
    @FXML private TableColumn<Consultation, String> patientNameColumn;
    @FXML private TableColumn<Consultation, String> startTimeColumn;
    @FXML private TableColumn<Consultation, String> endTimeColumn;
    @FXML private TableColumn<Consultation, String> diagnosisColumn;
    
    @FXML private Button createButton;
    @FXML private Button editButton;
    @FXML private Button viewButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    
    private final User currentUser;
    private final ServiceRegistry services;
    private final ConsultationService consultationService;
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final ExecutorService executorService;
    
    private final ObservableList<Consultation> consultationData = FXCollections.observableArrayList();

    public DoctorConsultationManagementController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.consultationService = services.getConsultationService();
        this.appointmentService = services.getAppointmentService();
        this.patientService = services.getPatientService();
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadConsultations();
        
        // Disable action buttons until selection
        editButton.setDisable(true);
        viewButton.setDisable(true);
        deleteButton.setDisable(true);
        
        consultationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            viewButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
        
        // Enable double-click to view
        consultationTable.setRowFactory(tv -> {
            TableRow<Consultation> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    handleView();
                }
            });
            return row;
        });
    }
    
    private void setupTable() {
        consultationIdColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getConsultationId()).asObject());
        
        patientNameColumn.setCellValueFactory(cellData -> {
            try {
                int appointmentId = cellData.getValue().getAppointmentId();
                Optional<Appointment> appointment = appointmentService.getAppointmentById(appointmentId);
                if (appointment.isPresent()) {
                    int patientId = appointment.get().getPatientId();
                    Optional<Patient> patient = patientService.getPatientById(patientId);
                    if (patient.isPresent()) {
                        String name = patient.get().getFirstName() + " " + patient.get().getLastName();
                        return new SimpleStringProperty(name);
                    }
                }
            } catch (Exception e) {
                // Fallback
            }
            return new SimpleStringProperty("N/A");
        });
        
        startTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getStartTime() != null) {
                return new SimpleStringProperty(DateTimeUtil.format(cellData.getValue().getStartTime()));
            }
            return new SimpleStringProperty("N/A");
        });
        
        endTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEndTime() != null) {
                return new SimpleStringProperty(DateTimeUtil.format(cellData.getValue().getEndTime()));
            }
            return new SimpleStringProperty("N/A");
        });
        
        diagnosisColumn.setCellValueFactory(cellData -> {
            String diagnosis = cellData.getValue().getDiagnosis();
            if (diagnosis != null && !diagnosis.isEmpty()) {
                // Truncate if too long
                return new SimpleStringProperty(diagnosis.length() > 50 
                    ? diagnosis.substring(0, 50) + "..." 
                    : diagnosis);
            }
            return new SimpleStringProperty("N/A");
        });
        
        consultationTable.setItems(consultationData);
    }
    
    private void loadConsultations() {
        Task<List<Consultation>> loadTask = new Task<>() {
            @Override
            protected List<Consultation> call() throws Exception {
                int doctorId = currentUser.getPersonId();
                return consultationService.getConsultationsByDoctorId(doctorId);
            }
            
            @Override
            protected void succeeded() {
                consultationData.clear();
                consultationData.addAll(getValue());
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    UiUtils.showError("Failed to load consultations: " + getException().getMessage());
                });
            }
        };
        
        executorService.execute(loadTask);
    }
    
    @FXML
    private void handleCreate() {
        navigateToForm(null);
    }
    
    @FXML
    private void handleEdit() {
        Consultation selected = consultationTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            navigateToForm(selected);
        }
    }
    
    @FXML
    private void handleView() {
        Consultation selected = consultationTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                Stage stage = (Stage) viewButton.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/consultation-details.fxml"));
                loader.setControllerFactory(controllerClass -> {
                    if (controllerClass == ConsultationDetailsController.class) {
                        return new ConsultationDetailsController(services, currentUser, selected);
                    }
                    throw new IllegalStateException("Unexpected controller: " + controllerClass);
                });
                stage.getScene().setRoot(loader.load());
            } catch (Exception e) {
                UiUtils.showError("Navigation failed: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleDelete() {
        Consultation selected = consultationTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirmed = UiUtils.showConfirmation(
                "Confirm Deletion",
                "Are you sure you want to delete consultation #" + selected.getConsultationId() + "?"
            );
            
            if (confirmed) {
                Task<Void> deleteTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        consultationService.deleteConsultation(selected.getConsultationId());
                        return null;
                    }
                    
                    @Override
                    protected void succeeded() {
                        UiUtils.showInfo("Consultation deleted successfully");
                        loadConsultations();
                    }
                    
                    @Override
                    protected void failed() {
                        UiUtils.showError("Failed to delete consultation: " + getException().getMessage());
                    }
                };
                
                executorService.execute(deleteTask);
            }
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadConsultations();
    }
    
    @FXML
    private void handleBack() {
        try {
            executorService.shutdownNow();
            Stage stage = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/doctor-dashboard.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == DoctorDashboardController.class) {
                    return new DoctorDashboardController(services, currentUser);
                }
                throw new IllegalStateException("Unexpected controller: " + controllerClass);
            });
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            UiUtils.showError("Navigation failed: " + e.getMessage());
        }
    }
    
    private void navigateToForm(Consultation consultation) {
        try {
            Stage stage = (Stage) createButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/doctor-consultation-form.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == DoctorConsultationFormController.class) {
                    return new DoctorConsultationFormController(services, currentUser, consultation);
                }
                throw new IllegalStateException("Unexpected controller: " + controllerClass);
            });
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            UiUtils.showError("Navigation failed: " + e.getMessage());
        }
    }
}
