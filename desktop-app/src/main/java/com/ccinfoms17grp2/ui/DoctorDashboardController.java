package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.AppointmentStatus;
import com.ccinfoms17grp2.models.Consultation;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class DoctorDashboardController implements ViewController {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d yyyy h:mm a");
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private SceneNavigator navigator;
    private ServiceRegistry services;
    private SessionContext session;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<Appointment> appointmentList;

    @FXML
    private ListView<Consultation> consultationList;

    @FXML
    private void initialize() {
        appointmentList.setCellFactory(list -> new ListCell<Appointment>() {
            @Override
            protected void updateItem(Appointment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatAppointment(item));
                }
            }
        });
        consultationList.setCellFactory(list -> new ListCell<Consultation>() {
            @Override
            protected void updateItem(Consultation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatConsultation(item));
                }
            }
        });
    }

    @FXML
    private void handleViewAppointments() {
        refreshAppointments();
    }

    @FXML
    private void handleConfirmAppointment() {
        Appointment selected = appointmentList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Select an appointment to confirm");
            return;
        }
        try {
            services.getAppointmentService().updateAppointmentStatus(selected.getAppointmentId(), AppointmentStatus.IN_PROGRESS);
            updateStatus("Appointment #" + selected.getAppointmentId() + " confirmed");
            refreshAppointments();
        } catch (RuntimeException ex) {
            updateStatus("Unable to confirm appointment");
            UiUtils.showError("Confirm Appointment", ex.getMessage());
        }
    }

    @FXML
    private void handleCancelAppointment() {
        Appointment selected = appointmentList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Select an appointment to cancel");
            return;
        }
        if (!UiUtils.showConfirmation("Cancel Appointment", "Cancel appointment #" + selected.getAppointmentId() + "?")) {
            return;
        }
        try {
            services.getAppointmentService().cancelAppointment(selected.getAppointmentId());
            updateStatus("Appointment #" + selected.getAppointmentId() + " canceled");
            refreshAppointments();
        } catch (RuntimeException ex) {
            updateStatus("Unable to cancel appointment");
            UiUtils.showError("Cancel Appointment", ex.getMessage());
        }
    }

    @FXML
    private void handleViewConsultations() {
        refreshConsultations();
    }

    @FXML
    private void handleCreateConsultation() {
        Appointment selected = appointmentList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Select an appointment to document");
            return;
        }
        Optional<String> diagnosis = promptText("Diagnosis", "Enter diagnosis details", "");
        if (!diagnosis.isPresent()) {
            updateStatus("Diagnosis is required");
            return;
        }
        Optional<String> plan = promptText("Treatment Plan", "Enter treatment plan (optional)", "");
        Optional<String> prescription = promptText("Prescription", "Enter prescription (optional)", "");
        Optional<LocalDateTime> followUp = promptFollowUp(null);
        try {
            Consultation consultation = new Consultation();
            consultation.setAppointmentId(selected.getAppointmentId());
            consultation.setStartTime(LocalDateTime.now());
            consultation.setEndTime(LocalDateTime.now().plusMinutes(30));
            consultation.setDiagnosis(diagnosis.get());
            consultation.setTreatmentPlan(plan.orElse(""));
            consultation.setPrescription(prescription.orElse(""));
            consultation.setFollowUpDate(followUp.orElse(null));
            services.getConsultationService().createConsultation(consultation);
            updateStatus("Consultation recorded for appointment #" + selected.getAppointmentId());
            refreshConsultations();
        } catch (RuntimeException ex) {
            updateStatus("Unable to create consultation");
            UiUtils.showError("Create Consultation", ex.getMessage());
        }
    }

    @FXML
    private void handleUpdateConsultation() {
        Consultation selected = consultationList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Select a consultation to update");
            return;
        }
        Optional<String> diagnosis = promptText("Diagnosis", "Update diagnosis", defaultString(selected.getDiagnosis()));
        if (!diagnosis.isPresent()) {
            updateStatus("Diagnosis is required");
            return;
        }
        Optional<String> plan = promptText("Treatment Plan", "Update treatment plan", defaultString(selected.getTreatmentPlan()));
        Optional<String> prescription = promptText("Prescription", "Update prescription", defaultString(selected.getPrescription()));
        Optional<LocalDateTime> followUp = promptFollowUp(selected.getFollowUpDate());
        try {
            selected.setDiagnosis(diagnosis.get());
            selected.setTreatmentPlan(plan.orElse(""));
            selected.setPrescription(prescription.orElse(""));
            selected.setFollowUpDate(followUp.orElse(null));
            services.getConsultationService().updateConsultation(selected);
            updateStatus("Consultation #" + selected.getConsultationId() + " updated");
            refreshConsultations();
        } catch (RuntimeException ex) {
            updateStatus("Unable to update consultation");
            UiUtils.showError("Update Consultation", ex.getMessage());
        }
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
        appointmentList.getItems().clear();
        consultationList.getItems().clear();
        if (session == null || !session.isUserType(User.UserType.DOCTOR) || services == null) {
            statusLabel.setText("Sign in as a doctor to continue");
            return;
        }
        statusLabel.setText("Daily queue overview");
        refreshAppointments();
    }

    private void refreshAppointments() {
        if (!ensureDoctorSession()) {
            return;
        }
        try {
            List<Appointment> appointments = services.getAppointmentService().getAppointmentsByDoctorId(session.getPersonIdOrThrow(User.UserType.DOCTOR));
            appointmentList.setItems(FXCollections.observableArrayList(appointments));
            if (appointments.isEmpty()) {
                updateStatus("No scheduled appointments");
            } else {
                updateStatus("Loaded " + appointments.size() + " appointments");
            }
        } catch (RuntimeException ex) {
            updateStatus("Unable to load appointments");
            UiUtils.showError("Doctor Appointments", ex.getMessage());
        }
    }

    private void refreshConsultations() {
        if (!ensureDoctorSession()) {
            return;
        }
        try {
            List<Consultation> consultations = services.getConsultationService().getConsultationsByDoctorId(session.getPersonIdOrThrow(User.UserType.DOCTOR));
            consultationList.setItems(FXCollections.observableArrayList(consultations));
            if (consultations.isEmpty()) {
                updateStatus("No consultations recorded");
            } else {
                updateStatus("Loaded " + consultations.size() + " consultations");
            }
        } catch (RuntimeException ex) {
            updateStatus("Unable to load consultations");
            UiUtils.showError("Doctor Consultations", ex.getMessage());
        }
    }

    private Optional<String> promptText(String title, String header, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(title + ":");
        Optional<String> value = dialog.showAndWait();
        if (!value.isPresent()) {
            return Optional.empty();
        }
        String trimmed = value.get().trim();
        if (trimmed.isEmpty() && defaultValue.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(trimmed);
    }

    private Optional<LocalDateTime> promptFollowUp(LocalDateTime current) {
        TextInputDialog dialog = new TextInputDialog(current == null ? "" : current.format(INPUT_FORMAT));
        dialog.setTitle("Follow-up");
        dialog.setHeaderText("Enter follow-up date/time (optional, yyyy-MM-dd HH:mm)");
        Optional<String> value = dialog.showAndWait();
        if (!value.isPresent() || value.get().trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDateTime.parse(value.get().trim(), INPUT_FORMAT));
        } catch (DateTimeParseException ex) {
            updateStatus("Invalid follow-up date");
            UiUtils.showError("Follow-up", "Use format yyyy-MM-dd HH:mm");
            return Optional.empty();
        }
    }

    private boolean ensureDoctorSession() {
        if (session == null || !session.isUserType(User.UserType.DOCTOR) || services == null) {
            updateStatus("Doctor session required");
            return false;
        }
        return true;
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private String formatAppointment(Appointment appointment) {
        String time = appointment.getAppointmentDateTime() == null ? "" : appointment.getAppointmentDateTime().format(DISPLAY_FORMAT);
        return "#" + appointment.getAppointmentId() + " " + time + " (" + appointment.getStatus() + ")";
    }

    private String formatConsultation(Consultation consultation) {
        String time = consultation.getStartTime() == null ? "" : consultation.getStartTime().format(DISPLAY_FORMAT);
        return "Consultation #" + consultation.getConsultationId() + " for appointment #" + consultation.getAppointmentId() + " " + time;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
