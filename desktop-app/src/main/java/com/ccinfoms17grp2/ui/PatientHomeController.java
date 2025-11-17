package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.Consultation;
import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

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
        appointmentsList.setCellFactory(list -> new ListCell<Appointment>() {
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
        consultationsList.setCellFactory(list -> new ListCell<Consultation>() {
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

    private String formatConsultation(Consultation consultation) {
        String time = consultation.getStartTime() == null ? "" : consultation.getStartTime().format(DISPLAY_FORMAT);
        return "Consultation #" + consultation.getConsultationId() + " for appointment #" + consultation.getAppointmentId() + " " + time;
    }
}
