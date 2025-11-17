package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.Consultation;
import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.AppointmentService;
import com.ccinfoms17grp2.services.ConsultationService;
import com.ccinfoms17grp2.services.PatientService;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.ValidationException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorConsultationFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private ComboBox<Appointment> appointmentComboBox;
    @FXML private Label patientInfoLabel;
    
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startTimeField;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField endTimeField;
    
    @FXML private TextArea diagnosisTextArea;
    @FXML private TextArea treatmentPlanTextArea;
    @FXML private TextArea prescriptionTextArea;
    
    @FXML private DatePicker followUpDatePicker;
    @FXML private TextField followUpTimeField;
    
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private final User currentUser;
    private final ServiceRegistry services;
    private final ConsultationService consultationService;
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final ExecutorService executorService;
    private final Consultation existingConsultation;
    
    private final ObservableList<Appointment> appointmentData = FXCollections.observableArrayList();

    public DoctorConsultationFormController(ServiceRegistry services, User currentUser, Consultation consultation) {
        this.services = services;
        this.currentUser = currentUser;
        this.consultationService = services.getConsultationService();
        this.appointmentService = services.getAppointmentService();
        this.patientService = services.getPatientService();
        this.executorService = Executors.newCachedThreadPool();
        this.existingConsultation = consultation;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleLabel.setText(existingConsultation == null ? "Create Consultation" : "Edit Consultation");
        
        setupAppointmentComboBox();
        loadDoctorAppointments();
        
        if (existingConsultation != null) {
            populateForm();
        } else {
            // Set default start time to now
            startDatePicker.setValue(LocalDate.now());
            startTimeField.setText(LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        }
        
        // Add listener to update patient info when appointment is selected
        appointmentComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updatePatientInfo(newVal);
            }
        });
    }
    
    private void setupAppointmentComboBox() {
        appointmentComboBox.setItems(appointmentData);
        appointmentComboBox.setButtonCell(new ListCell<Appointment>() {
            @Override
            protected void updateItem(Appointment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Appointment");
                } else {
                    setText("Appointment #" + item.getAppointmentId() + " - " + 
                        item.getAppointmentDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });
        appointmentComboBox.setCellFactory(lv -> new ListCell<Appointment>() {
            @Override
            protected void updateItem(Appointment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Appointment #" + item.getAppointmentId() + " - " + 
                        item.getAppointmentDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                        " (" + item.getStatus() + ")");
                }
            }
        });
    }
    
    private void loadDoctorAppointments() {
        Task<List<Appointment>> loadTask = new Task<>() {
            @Override
            protected List<Appointment> call() throws Exception {
                int doctorId = currentUser.getPersonId();
                return appointmentService.getAppointmentsByDoctorId(doctorId);
            }
            
            @Override
            protected void succeeded() {
                appointmentData.clear();
                appointmentData.addAll(getValue());
            }
            
            @Override
            protected void failed() {
                UiUtils.showError("Failed to load appointments: " + getException().getMessage());
            }
        };
        
        executorService.execute(loadTask);
    }
    
    private void updatePatientInfo(Appointment appointment) {
        Task<String> loadTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                Optional<Patient> patient = patientService.getPatientById(appointment.getPatientId());
                if (patient.isPresent()) {
                    Patient p = patient.get();
                    return "Patient: " + p.getFirstName() + " " + p.getLastName() + 
                           " | Contact: " + (p.getContactNumber() != null ? p.getContactNumber() : "N/A");
                }
                return "Patient ID: " + appointment.getPatientId();
            }
            
            @Override
            protected void succeeded() {
                patientInfoLabel.setText(getValue());
            }
            
            @Override
            protected void failed() {
                patientInfoLabel.setText("Patient ID: " + appointment.getPatientId());
            }
        };
        
        executorService.execute(loadTask);
    }
    
    private void populateForm() {
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Optional<Appointment> appointment = appointmentService.getAppointmentById(existingConsultation.getAppointmentId());
                
                Platform.runLater(() -> {
                    if (appointment.isPresent()) {
                        appointmentComboBox.setValue(appointment.get());
                        appointmentComboBox.setDisable(true);
                    }
                    
                    if (existingConsultation.getStartTime() != null) {
                        startDatePicker.setValue(existingConsultation.getStartTime().toLocalDate());
                        startTimeField.setText(existingConsultation.getStartTime().toLocalTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                    }
                    
                    if (existingConsultation.getEndTime() != null) {
                        endDatePicker.setValue(existingConsultation.getEndTime().toLocalDate());
                        endTimeField.setText(existingConsultation.getEndTime().toLocalTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                    }
                    
                    diagnosisTextArea.setText(existingConsultation.getDiagnosis() != null ? existingConsultation.getDiagnosis() : "");
                    treatmentPlanTextArea.setText(existingConsultation.getTreatmentPlan() != null ? existingConsultation.getTreatmentPlan() : "");
                    prescriptionTextArea.setText(existingConsultation.getPrescription() != null ? existingConsultation.getPrescription() : "");
                    
                    if (existingConsultation.getFollowUpDate() != null) {
                        followUpDatePicker.setValue(existingConsultation.getFollowUpDate().toLocalDate());
                        followUpTimeField.setText(existingConsultation.getFollowUpDate().toLocalTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                    }
                });
                
                return null;
            }
        };
        
        executorService.execute(loadTask);
    }
    
    @FXML
    private void handleSave() {
        try {
            // Validate
            Appointment selectedAppointment = appointmentComboBox.getValue();
            if (selectedAppointment == null) {
                UiUtils.showError("Please select an appointment");
                return;
            }
            
            if (diagnosisTextArea.getText() == null || diagnosisTextArea.getText().trim().isEmpty()) {
                UiUtils.showError("Diagnosis is required");
                return;
            }
            
            // Parse times
            LocalDateTime startTime = parseDateTime(startDatePicker.getValue(), startTimeField.getText());
            LocalDateTime endTime = parseDateTime(endDatePicker.getValue(), endTimeField.getText());
            LocalDateTime followUpDate = parseDateTime(followUpDatePicker.getValue(), followUpTimeField.getText());
            
            // Create/update consultation
            Consultation consultation;
            if (existingConsultation == null) {
                consultation = new Consultation();
                consultation.setAppointmentId(selectedAppointment.getAppointmentId());
            } else {
                consultation = existingConsultation;
            }
            
            consultation.setStartTime(startTime);
            consultation.setEndTime(endTime);
            consultation.setDiagnosis(diagnosisTextArea.getText().trim());
            consultation.setTreatmentPlan(treatmentPlanTextArea.getText().trim());
            consultation.setPrescription(prescriptionTextArea.getText().trim());
            consultation.setFollowUpDate(followUpDate);
            
            // Save
            Task<Consultation> saveTask = new Task<>() {
                @Override
                protected Consultation call() throws Exception {
                    if (existingConsultation == null) {
                        return consultationService.createConsultation(consultation);
                    } else {
                        return consultationService.updateConsultation(consultation);
                    }
                }
                
                @Override
                protected void succeeded() {
                    UiUtils.showInfo("Consultation saved successfully");
                    handleCancel();
                }
                
                @Override
                protected void failed() {
                    Throwable ex = getException();
                    if (ex instanceof ValidationException) {
                        UiUtils.showError(ex.getMessage());
                    } else {
                        UiUtils.showError("Failed to save consultation: " + ex.getMessage());
                    }
                }
            };
            
            executorService.execute(saveTask);
            
        } catch (Exception e) {
            UiUtils.showError("Invalid input: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        try {
            executorService.shutdownNow();
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/doctor-consultation-management.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == DoctorConsultationManagementController.class) {
                    return new DoctorConsultationManagementController(services, currentUser);
                }
                throw new IllegalStateException("Unexpected controller: " + controllerClass);
            });
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            UiUtils.showError("Navigation failed: " + e.getMessage());
        }
    }
    
    private LocalDateTime parseDateTime(LocalDate date, String timeStr) {
        if (date == null || timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            LocalTime time = LocalTime.parse(timeStr.trim(), java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(date, time);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid time format. Use HH:mm (e.g., 14:30)");
        }
    }
}
