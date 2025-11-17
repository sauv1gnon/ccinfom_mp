package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.models.Queue;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.AppointmentService;
import com.ccinfoms17grp2.services.DoctorService;
import com.ccinfoms17grp2.services.PatientService;
import com.ccinfoms17grp2.services.QueueService;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DoctorDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label queueCountLabel;
    @FXML private Label appointmentCountLabel;
    
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, Integer> appointmentIdColumn;
    @FXML private TableColumn<Appointment, String> patientNameColumn;
    @FXML private TableColumn<Appointment, String> appointmentTimeColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    
    @FXML private Button manageQueueButton;
    @FXML private Button manageConsultationsButton;
    @FXML private Button manageProfileButton;
    @FXML private Button refreshButton;
    @FXML private Button signOutButton;
    
    private final User currentUser;
    private final ServiceRegistry services;
    private final AppointmentService appointmentService;
    private final QueueService queueService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final ExecutorService executorService;
    
    private final ObservableList<Appointment> appointmentData = FXCollections.observableArrayList();

    public DoctorDashboardController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.appointmentService = services.getAppointmentService();
        this.queueService = services.getQueueService();
        this.doctorService = services.getDoctorService();
        this.patientService = services.getPatientService();
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupAppointmentsTable();
        loadDoctorData();
        loadDashboardData();
    }
    
    private void setupAppointmentsTable() {
        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        
        patientNameColumn.setCellValueFactory(cellData -> {
            int patientId = cellData.getValue().getPatientId();
            try {
                Optional<Patient> patient = patientService.getPatientById(patientId);
                if (patient.isPresent()) {
                    String name = patient.get().getFirstName() + " " + patient.get().getLastName();
                    return new SimpleStringProperty(name);
                }
            } catch (Exception e) {
                // Ignore and return ID
            }
            return new SimpleStringProperty("Patient #" + patientId);
        });
        
        appointmentTimeColumn.setCellValueFactory(cellData -> {
            String formatted = DateTimeUtil.format(cellData.getValue().getAppointmentDateTime());
            return new SimpleStringProperty(formatted);
        });
        
        statusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus().toString();
            return new SimpleStringProperty(status);
        });
        
        appointmentsTable.setItems(appointmentData);
    }
    
    private void loadDoctorData() {
        Task<Doctor> loadTask = new Task<>() {
            @Override
            protected Doctor call() throws Exception {
                int doctorId = currentUser.getPersonId();
                Optional<Doctor> doctor = doctorService.getDoctorById(doctorId);
                return doctor.orElse(null);
            }
            
            @Override
            protected void succeeded() {
                Doctor doctor = getValue();
                if (doctor != null) {
                    welcomeLabel.setText("Welcome, Dr. " + doctor.getFirstName() + " " + doctor.getLastName());
                } else {
                    welcomeLabel.setText("Welcome, Doctor");
                }
            }
            
            @Override
            protected void failed() {
                welcomeLabel.setText("Welcome, Doctor");
            }
        };
        
        executorService.execute(loadTask);
    }
    
    private void loadDashboardData() {
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int doctorId = currentUser.getPersonId();
                
                // Load today's appointments
                List<Appointment> appointments = appointmentService.getTodaysAppointmentsByDoctor(doctorId);
                
                // Load today's queue count
                List<Queue> queues = queueService.listTodaysQueue();
                
                Platform.runLater(() -> {
                    appointmentData.clear();
                    appointmentData.addAll(appointments);
                    appointmentCountLabel.setText("Today's Appointments: " + appointments.size());
                    queueCountLabel.setText("Queue Waiting: " + queues.stream()
                        .filter(q -> q.getStatus().getValue().equals("waiting")).count());
                });
                
                return null;
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    UiUtils.showError("Failed to load dashboard data: " + getException().getMessage());
                });
            }
        };
        
        executorService.execute(loadTask);
    }
    
    @FXML
    private void handleManageQueue() {
        navigateTo("/com/ccinfoms17grp2/ui/doctor-queue-management.fxml", DoctorQueueManagementController.class);
    }
    
    @FXML
    private void handleManageConsultations() {
        navigateTo("/com/ccinfoms17grp2/ui/doctor-consultation-management.fxml", DoctorConsultationManagementController.class);
    }
    
    @FXML
    private void handleManageProfile() {
        navigateTo("/com/ccinfoms17grp2/ui/doctor-profile-management.fxml", DoctorProfileManagementController.class);
    }
    
    @FXML
    private void handleRefresh() {
        loadDashboardData();
    }
    
    @FXML
    private void handleSignOut() {
        try {
            executorService.shutdownNow();
            Stage stage = (Stage) signOutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/login.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == LoginController.class) {
                    return new LoginController(services);
                }
                throw new IllegalStateException("Unexpected controller: " + controllerClass);
            });
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            UiUtils.showError("Failed to sign out: " + e.getMessage());
        }
    }
    
    private void navigateTo(String fxmlPath, Class<?> controllerClass) {
        try {
            Stage stage = (Stage) manageQueueButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(clazz -> {
                if (clazz == controllerClass) {
                    try {
                        return controllerClass
                            .getConstructor(ServiceRegistry.class, User.class)
                            .newInstance(services, currentUser);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create controller: " + e.getMessage(), e);
                    }
                }
                throw new IllegalStateException("Unexpected controller: " + clazz);
            });
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            UiUtils.showError("Navigation failed: " + e.getMessage());
        }
    }
}
