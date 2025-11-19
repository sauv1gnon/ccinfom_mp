package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.AppointmentStatus;
import com.ccinfoms17grp2.models.Consultation;
import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DoctorDashboardController implements ViewController {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d yyyy h:mm a");

    private SceneNavigator navigator;
    private ServiceRegistry services;
    private SessionContext session;

    @FXML
    private Label welcomeLabel;

    @FXML
    private FlowPane appointmentCardsContainer;

    @FXML
    private FlowPane consultationCardsContainer;

    @FXML
    private void initialize() {
        if (appointmentCardsContainer != null) {
            appointmentCardsContainer.setStyle("-fx-padding: 0;");
        }
        if (consultationCardsContainer != null) {
            consultationCardsContainer.setStyle("-fx-padding: 0;");
        }
    }

    @FXML
    private void handleViewAppointments() {
        refreshAppointments();
    }

    @FXML
    private void handleConfirmAppointment() {
        UiUtils.showInfo("Info", "Feature coming soon");
    }

    @FXML
    private void handleCancelAppointment() {
        UiUtils.showInfo("Info", "Feature coming soon");
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
        appointmentCardsContainer.getChildren().clear();
        consultationCardsContainer.getChildren().clear();
        if (session == null || !session.isUserType(User.UserType.DOCTOR) || services == null) {
            welcomeLabel.setText("Sign in as a doctor to continue");
            return;
        }
        
        // Get doctor info
        try {
            int doctorId = session.getPersonIdOrThrow(User.UserType.DOCTOR);
            Optional<Doctor> doctorOpt = services.getDoctorService().getDoctorById(doctorId);
            if (doctorOpt.isPresent()) {
                Doctor doctor = doctorOpt.get();
                String displayName = doctor.getFirstName() + " " + doctor.getLastName();
                welcomeLabel.setText("Welcome, Dr. " + displayName + ".");
            } else {
                welcomeLabel.setText("Welcome, Doctor.");
            }
        } catch (Exception ex) {
            welcomeLabel.setText("Welcome, Doctor.");
        }
        
        refreshAppointments();
        refreshConsultations();
    }

    private void refreshAppointments() {
        if (!ensureDoctorSession()) {
            return;
        }
        try {
            List<Appointment> appointments = services.getAppointmentService()
                    .getAppointmentsByDoctorId(session.getPersonIdOrThrow(User.UserType.DOCTOR));
            appointmentCardsContainer.getChildren().clear();
            for (Appointment appointment : appointments) {
                VBox card = createAppointmentCard(appointment);
                appointmentCardsContainer.getChildren().add(card);
            }
        } catch (RuntimeException ex) {
            UiUtils.showError("Doctor Appointments", ex.getMessage());
        }
    }

    private void refreshConsultations() {
        if (!ensureDoctorSession()) {
            return;
        }
        try {
            List<Consultation> consultations = services.getConsultationService()
                    .getConsultationsByDoctorId(session.getPersonIdOrThrow(User.UserType.DOCTOR));
            
            consultationCardsContainer.getChildren().clear();
            if (consultations.isEmpty()) {
                Label emptyLabel = new Label("No consultations recorded yet");
                emptyLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 16; -fx-text-fill: #757575;");
                consultationCardsContainer.getChildren().add(emptyLabel);
            } else {
                for (Consultation consultation : consultations) {
                    VBox card = createConsultationCard(consultation);
                    consultationCardsContainer.getChildren().add(card);
                }
            }
        } catch (RuntimeException ex) {
            consultationCardsContainer.getChildren().clear();
            Label emptyLabel = new Label("No consultations recorded yet");
            emptyLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 16; -fx-text-fill: #757575;");
            consultationCardsContainer.getChildren().add(emptyLabel);
        }
    }

    private VBox createAppointmentCard(Appointment appointment) {
        VBox card = new VBox();
        card.setStyle("-fx-border-color: #d9d9d9; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-background-color: #ffffff; -fx-padding: 24; -fx-min-width: 450; -fx-max-width: 450; " +
                "-fx-min-height: 140; -fx-spacing: 24; -fx-cursor: hand;");
        
        Label statusLabel = new Label(appointment.getStatus().toString());
        statusLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 24; -fx-font-weight: 600; " +
                "-fx-text-fill: #1e1e1e;");
        
        VBox infoBox = new VBox();
        infoBox.setStyle("-fx-spacing: 2;");
        
        // Get patient name
        Label patientNameLabel = new Label();
        try {
            Optional<Patient> patientOpt = services.getPatientService().getPatientById(appointment.getPatientId());
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
            } else {
                patientNameLabel.setText("Patient ID: " + appointment.getPatientId());
            }
        } catch (Exception ex) {
            patientNameLabel.setText("Patient ID: " + appointment.getPatientId());
        }
        patientNameLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 16; -fx-font-weight: 600; " +
                "-fx-text-fill: #757575;");
        
        Label dateLabel = new Label();
        if (appointment.getAppointmentDateTime() != null) {
            dateLabel.setText(appointment.getAppointmentDateTime().format(DISPLAY_FORMAT));
        } else {
            dateLabel.setText("Date/Time pending");
        }
        dateLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 16; -fx-font-weight: 400; " +
                "-fx-text-fill: #b3b3b3;");
        
        infoBox.getChildren().addAll(patientNameLabel, dateLabel);
        card.getChildren().addAll(statusLabel, infoBox);
        
        // Add click handler
        card.setOnMouseClicked(event -> handleAppointmentCardClick(appointment));
        
        // Add hover effect
        card.setOnMouseEntered(event -> card.setStyle(card.getStyle() + " -fx-background-color: #f5f5f5;"));
        card.setOnMouseExited(event -> card.setStyle(card.getStyle().replace("-fx-background-color: #f5f5f5;", "-fx-background-color: #ffffff;")));
        
        return card;
    }
    
    private void handleAppointmentCardClick(Appointment appointment) {
        // Only allow modifications for scheduled appointments
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            UiUtils.showInfo("Appointment Status", "Only scheduled appointments can be modified. This appointment is already " + appointment.getStatus().toString().toLowerCase() + ".");
            return;
        }
        
        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>("Complete Appointment",
                "Complete Appointment", "Cancel Appointment");
        dialog.setTitle("Appointment Action");
        dialog.setHeaderText("What would you like to do with this appointment?");
        dialog.setContentText("Choose action:");
        
        java.util.Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            if (result.get().equals("Cancel Appointment")) {
                if (UiUtils.showConfirmation("Confirm", "Are you sure you want to cancel this appointment?")) {
                    try {
                        services.getAppointmentService().cancelAppointment(appointment.getAppointmentId());
                        UiUtils.showInfo("Success", "Appointment canceled successfully");
                        refreshAppointments();
                    } catch (RuntimeException ex) {
                        UiUtils.showError("Cancel Appointment", ex.getMessage());
                    }
                }
            } else if (result.get().equals("Complete Appointment")) {
                // Show consultation dialog when completing appointment
                showConsultationDialog(appointment);
            }
        }
    }

    private VBox createConsultationCard(Consultation consultation) {
        VBox card = new VBox();
        card.setStyle("-fx-border-color: #d9d9d9; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-background-color: #ffffff; -fx-padding: 24; -fx-min-width: 630; -fx-max-width: 630; " +
                "-fx-min-height: 185; -fx-spacing: 24; -fx-cursor: hand;");
        
        Label timeLabel = new Label();
        if (consultation.getStartTime() != null && consultation.getEndTime() != null) {
            timeLabel.setText(consultation.getStartTime().format(DISPLAY_FORMAT) + " to " +
                    consultation.getEndTime().format(DISPLAY_FORMAT));
        } else {
            timeLabel.setText("Time pending");
        }
        timeLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 24; -fx-font-weight: 600; " +
                "-fx-text-fill: #1e1e1e;");
        
        VBox infoBox = new VBox();
        infoBox.setStyle("-fx-spacing: 2;");
        
        Label appointmentIdLabel = new Label("Appointment ID: " + consultation.getAppointmentId());
        appointmentIdLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 16; -fx-font-weight: 400; " +
                "-fx-text-fill: #1e1e1e;");
        
        Label diagnosisLabel = new Label(consultation.getDiagnosis() != null ? 
                consultation.getDiagnosis() : "Diagnosis pending");
        diagnosisLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 16; -fx-font-weight: 400; " +
                "-fx-text-fill: #1e1e1e;");
        diagnosisLabel.setWrapText(true);
        
        Label treatmentLabel = new Label("Treatment: " + (consultation.getTreatmentPlan() != null ? 
                consultation.getTreatmentPlan() : "Plan pending"));
        treatmentLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 16; -fx-font-weight: 600; " +
                "-fx-text-fill: #757575;");
        treatmentLabel.setWrapText(true);
        
        Label followUpLabel = new Label();
        if (consultation.getFollowUpDate() != null) {
            followUpLabel.setText("Follow-up on " + consultation.getFollowUpDate().format(DISPLAY_FORMAT));
        } else {
            followUpLabel.setText("No follow-up scheduled");
        }
        followUpLabel.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 16; -fx-font-weight: 400; " +
                "-fx-text-fill: #b3b3b3;");
        
        infoBox.getChildren().addAll(appointmentIdLabel, diagnosisLabel, treatmentLabel, followUpLabel);
        card.getChildren().addAll(timeLabel, infoBox);
        
        // Add click handler
        card.setOnMouseClicked(event -> handleConsultationCardClick(consultation));
        
        // Add hover effect
        card.setOnMouseEntered(event -> card.setStyle(card.getStyle() + " -fx-background-color: #f5f5f5;"));
        card.setOnMouseExited(event -> card.setStyle(card.getStyle().replace("-fx-background-color: #f5f5f5;", "-fx-background-color: #ffffff;")));
        
        return card;
    }
    
    private void handleConsultationCardClick(Consultation consultation) {
        // Check if consultation can be updated (current datetime should not exceed end_time)
        LocalDateTime now = LocalDateTime.now();
        boolean canUpdate = consultation.getEndTime() == null || !now.isAfter(consultation.getEndTime());
        
        if (canUpdate) {
            // Show update dialog
            showConsultationUpdateDialog(consultation);
        } else {
            // Show read-only details
            StringBuilder details = new StringBuilder();
            details.append("Consultation ID: ").append(consultation.getConsultationId()).append("\n");
            details.append("Appointment ID: ").append(consultation.getAppointmentId()).append("\n\n");
            
            if (consultation.getStartTime() != null) {
                details.append("Start Time: ").append(consultation.getStartTime().format(DISPLAY_FORMAT)).append("\n");
            }
            if (consultation.getEndTime() != null) {
                details.append("End Time: ").append(consultation.getEndTime().format(DISPLAY_FORMAT)).append("\n\n");
            }
            
            details.append("Diagnosis:\n").append(consultation.getDiagnosis() != null ? consultation.getDiagnosis() : "N/A").append("\n\n");
            details.append("Treatment Plan:\n").append(consultation.getTreatmentPlan() != null ? consultation.getTreatmentPlan() : "N/A").append("\n\n");
            details.append("Prescription:\n").append(consultation.getPrescription() != null ? consultation.getPrescription() : "N/A").append("\n\n");
            
            if (consultation.getFollowUpDate() != null) {
                details.append("Follow-up Date: ").append(consultation.getFollowUpDate().format(DISPLAY_FORMAT));
            } else {
                details.append("Follow-up Date: Not scheduled");
            }
            
            UiUtils.showInfo("Consultation Details (Read-Only)", details.toString());
        }
    }

    private boolean ensureDoctorSession() {
        return session != null && session.isUserType(User.UserType.DOCTOR) && services != null;
    }
    
    private void showConsultationDialog(Appointment appointment) {
        Dialog<Consultation> dialog = new Dialog<>();
        dialog.setTitle("Create Consultation");
        dialog.setHeaderText("Enter consultation details for Appointment #" + appointment.getAppointmentId());
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField startTimeField = new TextField();
        startTimeField.setPromptText("YYYY-MM-DD HH:MM");
        startTimeField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        TextField endTimeField = new TextField();
        endTimeField.setPromptText("YYYY-MM-DD HH:MM");
        endTimeField.setText(LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        TextArea diagnosisArea = new TextArea();
        diagnosisArea.setPromptText("Enter diagnosis...");
        diagnosisArea.setPrefRowCount(3);
        
        TextArea treatmentPlanArea = new TextArea();
        treatmentPlanArea.setPromptText("Enter treatment plan...");
        treatmentPlanArea.setPrefRowCount(3);
        
        TextArea prescriptionArea = new TextArea();
        prescriptionArea.setPromptText("Enter prescription...");
        prescriptionArea.setPrefRowCount(3);
        
        TextField followUpDateField = new TextField();
        followUpDateField.setPromptText("YYYY-MM-DD HH:MM (optional)");
        
        grid.add(new Label("Start Time:"), 0, 0);
        grid.add(startTimeField, 1, 0);
        grid.add(new Label("End Time:"), 0, 1);
        grid.add(endTimeField, 1, 1);
        grid.add(new Label("Diagnosis:"), 0, 2);
        grid.add(diagnosisArea, 1, 2);
        grid.add(new Label("Treatment Plan:"), 0, 3);
        grid.add(treatmentPlanArea, 1, 3);
        grid.add(new Label("Prescription:"), 0, 4);
        grid.add(prescriptionArea, 1, 4);
        grid.add(new Label("Follow-up Date:"), 0, 5);
        grid.add(followUpDateField, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Consultation consultation = new Consultation();
                    consultation.setAppointmentId(appointment.getAppointmentId());
                    
                    String startTimeStr = startTimeField.getText().trim();
                    if (!startTimeStr.isEmpty()) {
                        consultation.setStartTime(LocalDateTime.parse(startTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    }
                    
                    String endTimeStr = endTimeField.getText().trim();
                    if (!endTimeStr.isEmpty()) {
                        consultation.setEndTime(LocalDateTime.parse(endTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    }
                    
                    consultation.setDiagnosis(diagnosisArea.getText().trim());
                    consultation.setTreatmentPlan(treatmentPlanArea.getText().trim());
                    consultation.setPrescription(prescriptionArea.getText().trim());
                    
                    String followUpStr = followUpDateField.getText().trim();
                    if (!followUpStr.isEmpty()) {
                        consultation.setFollowUpDate(LocalDateTime.parse(followUpStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    }
                    
                    return consultation;
                } catch (Exception e) {
                    UiUtils.showError("Invalid Input", "Please check the date/time format (YYYY-MM-DD HH:MM)");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Consultation> result = dialog.showAndWait();
        
        result.ifPresent(consultation -> {
            try {
                services.getConsultationService().createConsultation(consultation);
                services.getAppointmentService().updateAppointmentStatus(
                        appointment.getAppointmentId(), 
                        AppointmentStatus.COMPLETED
                );
                UiUtils.showInfo("Success", "Consultation created and appointment marked as completed");
                refreshAppointments();
                refreshConsultations();
            } catch (RuntimeException ex) {
                UiUtils.showError("Create Consultation", ex.getMessage());
            }
        });
    }
    
    private void showConsultationUpdateDialog(Consultation consultation) {
        Dialog<Consultation> dialog = new Dialog<>();
        dialog.setTitle("Update Consultation");
        dialog.setHeaderText("Update consultation details for Consultation #" + consultation.getConsultationId());
        
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField startTimeField = new TextField();
        startTimeField.setPromptText("YYYY-MM-DD HH:MM");
        if (consultation.getStartTime() != null) {
            startTimeField.setText(consultation.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        
        TextField endTimeField = new TextField();
        endTimeField.setPromptText("YYYY-MM-DD HH:MM");
        if (consultation.getEndTime() != null) {
            endTimeField.setText(consultation.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        
        TextArea diagnosisArea = new TextArea();
        diagnosisArea.setPromptText("Enter diagnosis...");
        diagnosisArea.setPrefRowCount(3);
        if (consultation.getDiagnosis() != null) {
            diagnosisArea.setText(consultation.getDiagnosis());
        }
        
        TextArea treatmentPlanArea = new TextArea();
        treatmentPlanArea.setPromptText("Enter treatment plan...");
        treatmentPlanArea.setPrefRowCount(3);
        if (consultation.getTreatmentPlan() != null) {
            treatmentPlanArea.setText(consultation.getTreatmentPlan());
        }
        
        TextArea prescriptionArea = new TextArea();
        prescriptionArea.setPromptText("Enter prescription...");
        prescriptionArea.setPrefRowCount(3);
        if (consultation.getPrescription() != null) {
            prescriptionArea.setText(consultation.getPrescription());
        }
        
        TextField followUpDateField = new TextField();
        followUpDateField.setPromptText("YYYY-MM-DD HH:MM (optional)");
        if (consultation.getFollowUpDate() != null) {
            followUpDateField.setText(consultation.getFollowUpDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        
        grid.add(new Label("Start Time:"), 0, 0);
        grid.add(startTimeField, 1, 0);
        grid.add(new Label("End Time:"), 0, 1);
        grid.add(endTimeField, 1, 1);
        grid.add(new Label("Diagnosis:"), 0, 2);
        grid.add(diagnosisArea, 1, 2);
        grid.add(new Label("Treatment Plan:"), 0, 3);
        grid.add(treatmentPlanArea, 1, 3);
        grid.add(new Label("Prescription:"), 0, 4);
        grid.add(prescriptionArea, 1, 4);
        grid.add(new Label("Follow-up Date:"), 0, 5);
        grid.add(followUpDateField, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    Consultation updated = new Consultation();
                    updated.setConsultationId(consultation.getConsultationId());
                    updated.setAppointmentId(consultation.getAppointmentId());
                    
                    String startTimeStr = startTimeField.getText().trim();
                    if (!startTimeStr.isEmpty()) {
                        updated.setStartTime(LocalDateTime.parse(startTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    }
                    
                    String endTimeStr = endTimeField.getText().trim();
                    if (!endTimeStr.isEmpty()) {
                        updated.setEndTime(LocalDateTime.parse(endTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    }
                    
                    updated.setDiagnosis(diagnosisArea.getText().trim());
                    updated.setTreatmentPlan(treatmentPlanArea.getText().trim());
                    updated.setPrescription(prescriptionArea.getText().trim());
                    
                    String followUpStr = followUpDateField.getText().trim();
                    if (!followUpStr.isEmpty()) {
                        updated.setFollowUpDate(LocalDateTime.parse(followUpStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    }
                    
                    return updated;
                } catch (Exception e) {
                    UiUtils.showError("Invalid Input", "Please check the date/time format (YYYY-MM-DD HH:MM)");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Consultation> result = dialog.showAndWait();
        
        result.ifPresent(updated -> {
            try {
                services.getConsultationService().updateConsultation(updated);
                UiUtils.showInfo("Success", "Consultation updated successfully");
                refreshConsultations();
            } catch (RuntimeException ex) {
                UiUtils.showError("Update Consultation", ex.getMessage());
            }
        });
    }
}
