package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.Consultation;
import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.AppointmentStatus;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class PatientHomeController implements ViewController {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d yyyy h:mm a");

    private SceneNavigator navigator;
    private ServiceRegistry services;
    private SessionContext session;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<Appointment> appointmentsList;

    @FXML
    private ListView<Consultation> consultationsList;

    @FXML
    private void initialize() {
        // Setup appointments list with custom card rendering
        appointmentsList.setCellFactory(list -> new ListCell<Appointment>() {
            @Override
            protected void updateItem(Appointment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    setGraphic(createAppointmentCard(item));
                }
            }
        });
        
        // Setup consultations list with custom card rendering
        consultationsList.setCellFactory(list -> new ListCell<Consultation>() {
            @Override
            protected void updateItem(Consultation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(createConsultationCard(item));
                }
            }
        });
    }

    @FXML
    private void handleViewAppointments() {
        refreshAppointments();
    }

    @FXML
    private void handleCreateAppointment() {
        if (!ensurePatientSession()) {
            return;
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/ccinfoms17grp2/ui/appointment-booking.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            AppointmentBookingDialogController controller = loader.getController();
            controller.setServices(services);
            controller.setPatientId(requirePatientId());
            
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.setTitle("Book Appointment");
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.showAndWait();
            
            if (controller.getCreatedAppointment() != null) {
                updateStatus("Appointment created successfully");
                refreshAppointments();
            }
        } catch (Exception ex) {
            updateStatus("Unable to open appointment booking");
            UiUtils.showError("Create Appointment", ex.getMessage());
        }
    }

    @FXML
    private void handleCancelAppointment() {
        Appointment selected = appointmentsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Select an appointment to cancel");
            return;
        }

        if (selected.getStatus() != AppointmentStatus.SCHEDULED) {
            updateStatus("Only scheduled appointments can be cancelled");
            return;
        }

        if (!UiUtils.showConfirmation("Cancel Appointment", "Cancel appointment #" + selected.getAppointmentId() + "?")) {
            return;
        }
        try {
            boolean success = services.getAppointmentService().cancelAppointment(selected.getAppointmentId());
            if (success) {
                updateStatus("Appointment #" + selected.getAppointmentId() + " canceled");
                refreshAppointments();
            } else {
                updateStatus("Failed to cancel appointment - please try again");
            }
        } catch (RuntimeException ex) {
            updateStatus("Unable to cancel appointment: " + ex.getMessage());
            UiUtils.showError("Cancel Appointment", ex.getMessage());
        }
    }

    @FXML
    private void handleViewConsultations() {
        refreshConsultations();
    }

    @FXML
    private void handleBackToLogin() {
        if (session != null) {
            session.clear();
        }
        navigator.show(UiView.LOGIN);
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
    public void setSession(SessionContext session) {
        this.session = session;
    }

    @Override
    public void onDisplay() {
        statusLabel.setText("");
        appointmentsList.getItems().clear();
        consultationsList.getItems().clear();
        if (session == null || !session.isUserType(User.UserType.PATIENT) || services == null) {
            welcomeLabel.setText("Sign in as a patient to view records");
            return;
        }
        int patientId = session.getPersonIdOrThrow(User.UserType.PATIENT);
        Optional<Patient> patientOpt = services.getPatientService().getPatientById(patientId);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            welcomeLabel.setText("Welcome, " + patient.getFirstName() + " " + patient.getLastName());
        } else {
            welcomeLabel.setText("Welcome, patient #" + patientId);
        }
        refreshAppointments();
    }

    private void refreshAppointments() {
        if (!ensurePatientSession()) {
            return;
        }
        try {
            List<Appointment> appointments = services.getAppointmentService().getAppointmentsByPatientId(requirePatientId());
            appointmentsList.setItems(FXCollections.observableArrayList(appointments));
            if (appointments.isEmpty()) {
                updateStatus("No appointments scheduled");
            } else {
                updateStatus("Loaded " + appointments.size() + " appointments");
            }
        } catch (Exception ex) {
            appointmentsList.setItems(FXCollections.observableArrayList());
            updateStatus("No appointments available");
        }
    }

    private void refreshConsultations() {
        if (!ensurePatientSession()) {
            return;
        }
        try {
            List<Consultation> consultations = services.getConsultationService().getConsultationsByPatientId(requirePatientId());
            consultationsList.setItems(FXCollections.observableArrayList(consultations));
            if (consultations.isEmpty()) {
                updateStatus("No consultations recorded");
            } else {
                updateStatus("Loaded " + consultations.size() + " consultations");
            }
        } catch (Exception ex) {
            consultationsList.setItems(FXCollections.observableArrayList());
            updateStatus("No consultations available");
        }
    }

    private boolean ensurePatientSession() {
        if (session == null || !session.isUserType(User.UserType.PATIENT) || services == null) {
            updateStatus("Patient session required");
            return false;
        }
        return true;
    }

    private int requirePatientId() {
        return session.getPersonIdOrThrow(User.UserType.PATIENT);
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private String formatAppointment(Appointment appointment) {
        String time = appointment.getAppointmentDateTime() == null ? "" : appointment.getAppointmentDateTime().format(DISPLAY_FORMAT);
        return "#" + appointment.getAppointmentId() + " " + time + " (" + appointment.getStatus() + ")";
    }

    private VBox createAppointmentCard(Appointment appointment) {
        VBox card = new VBox(24.0);
        card.setStyle("-fx-border-color: #d9d9d9; -fx-border-radius: 8; -fx-background-color: #ffffff; -fx-padding: 24;");
        card.setPrefWidth(450);
        card.setPrefHeight(199);

        // Header with Status
        VBox headerBox = new VBox(4.0);
        
        Label appointmentStatusLabel = new Label(appointment.getStatus().toString());
        appointmentStatusLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 600; -fx-text-fill: #1e1e1e;");

        // Branch info
        Label branchLabel = new Label();
        Label addressLabel = new Label();
        try {
            Optional<Branch> branchOpt = services.getBranchService().getBranchById(appointment.getBranchId());
            if (branchOpt.isPresent()) {
                Branch branch = branchOpt.get();
                branchLabel.setText(branch.getBranchName());
                addressLabel.setText(branch.getAddress());
            }
        } catch (Exception e) {
            branchLabel.setText("Branch");
            addressLabel.setText("");
        }
        branchLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #1e1e1e;");
        addressLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #1e1e1e; -fx-wrap-text: true;");
        addressLabel.setWrapText(true);

        headerBox.getChildren().addAll(appointmentStatusLabel, branchLabel, addressLabel);

        // Doctor and DateTime info
        VBox infoBox = new VBox(2.0);
        
        Label doctorLabel = new Label();
        try {
            Optional<Doctor> doctorOpt = services.getDoctorService().getDoctorById(appointment.getDoctorId());
            if (doctorOpt.isPresent()) {
                Doctor doctor = doctorOpt.get();
                doctorLabel.setText("Dr. " + doctor.getFirstName() + " " + doctor.getLastName());
            }
        } catch (Exception e) {
            doctorLabel.setText("Dr. Unknown");
        }
        doctorLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #757575;");

        Label dateTimeLabel = new Label();
        if (appointment.getAppointmentDateTime() != null) {
            dateTimeLabel.setText(appointment.getAppointmentDateTime().format(DISPLAY_FORMAT));
        }
        dateTimeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b3b3b3;");

        infoBox.getChildren().addAll(doctorLabel, dateTimeLabel);

        card.getChildren().addAll(headerBox, infoBox);
        return card;
    }

    private VBox createConsultationCard(Consultation consultation) {
        VBox card = new VBox(24.0);
        card.setStyle("-fx-border-color: #d9d9d9; -fx-border-radius: 8; -fx-background-color: #ffffff; -fx-padding: 24;");
        card.setPrefWidth(630);
        card.setPrefHeight(160);

        // Time range
        VBox headerBox = new VBox(4.0);
        
        Label timeRangeLabel = new Label();
        if (consultation.getStartTime() != null && consultation.getEndTime() != null) {
            String startStr = consultation.getStartTime().format(DISPLAY_FORMAT);
            String endStr = consultation.getEndTime().format(DISPLAY_FORMAT);
            timeRangeLabel.setText(startStr + " to " + endStr);
        } else if (consultation.getStartTime() != null) {
            timeRangeLabel.setText(consultation.getStartTime().format(DISPLAY_FORMAT));
        }
        timeRangeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 600; -fx-text-fill: #1e1e1e;");

        // Diagnosis
        Label diagnosisLabel = new Label(consultation.getDiagnosis() != null ? consultation.getDiagnosis() : "");
        diagnosisLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #1e1e1e;");

        headerBox.getChildren().addAll(timeRangeLabel, diagnosisLabel);

        // Treatment plan and follow-up
        VBox infoBox = new VBox(2.0);
        
        Label treatmentLabel = new Label(consultation.getTreatmentPlan() != null ? consultation.getTreatmentPlan() : "");
        treatmentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #757575;");

        Label followUpLabel = new Label();
        if (consultation.getFollowUpDate() != null) {
            followUpLabel.setText("Follow-up on " + consultation.getFollowUpDate().format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")));
        } else {
            followUpLabel.setText("No follow-up scheduled");
        }
        followUpLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b3b3b3;");

        infoBox.getChildren().addAll(treatmentLabel, followUpLabel);

        card.getChildren().addAll(headerBox, infoBox);
        return card;
    }
}
