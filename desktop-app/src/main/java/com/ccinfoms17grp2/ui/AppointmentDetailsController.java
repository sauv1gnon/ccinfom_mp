package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.*;
import com.ccinfoms17grp2.services.*;
import com.ccinfoms17grp2.utils.DateTimeUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class AppointmentDetailsController implements Initializable {

    @FXML private Label appointmentIdLabel;
    @FXML private Label appointmentDateLabel;
    @FXML private Label doctorNameLabel;
    @FXML private Label branchNameLabel;
    @FXML private Label statusLabel;
    @FXML private Label createdAtLabel;

    private final User currentUser;
    private final ServiceRegistry services;
    private final Appointment appointment;

    public AppointmentDetailsController(ServiceRegistry services, User currentUser, Appointment appointment) {
        this.services = services;
        this.currentUser = currentUser;
        this.appointment = appointment;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAppointmentDetails();
    }

    private void loadAppointmentDetails() {
        appointmentIdLabel.setText(String.valueOf(appointment.getAppointmentId()));
        appointmentDateLabel.setText(DateTimeUtil.format(appointment.getAppointmentDateTime()));
        statusLabel.setText(appointment.getStatus().toString());
        createdAtLabel.setText(DateTimeUtil.format(appointment.getCreatedAt()));
        
        loadDoctorName();
        loadBranchName();
    }

    private void loadDoctorName() {
        try {
            Optional<Doctor> doctor = services.getDoctorService().listDoctors().stream()
                .filter(d -> d.getDoctorId() == appointment.getDoctorId())
                .findFirst();
            
            if (doctor.isPresent()) {
                doctorNameLabel.setText(doctor.get().getFirstName() + " " + doctor.get().getLastName());
            } else {
                doctorNameLabel.setText("Doctor ID: " + appointment.getDoctorId());
            }
        } catch (Exception ex) {
            doctorNameLabel.setText("Error loading doctor info");
        }
    }

    private void loadBranchName() {
        try {
            Optional<Branch> branch = services.getBranchService().listBranches().stream()
                .filter(b -> b.getBranchId() == appointment.getBranchId())
                .findFirst();
            
            if (branch.isPresent()) {
                branchNameLabel.setText(branch.get().getBranchName());
            } else {
                branchNameLabel.setText("Branch ID: " + appointment.getBranchId());
            }
        } catch (Exception ex) {
            branchNameLabel.setText("Error loading branch info");
        }
    }

    @FXML
    private void handleBackToAppointments() {
        navigateToAppointmentList();
    }

    @FXML
    private void handleBackToHomepage() {
        navigateToHomepage();
    }

    @FXML
    private void handleCancelAppointment() {
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            UiUtils.showWarning("Already Cancelled", "This appointment is already cancelled.");
            return;
        }

        boolean confirmed = UiUtils.showConfirmation(
            "Cancel Appointment",
            "Are you sure you want to cancel this appointment?"
        );

        if (confirmed) {
            try {
                services.getAppointmentService().cancelAppointment(appointment.getAppointmentId());
                appointment.setStatus(AppointmentStatus.CANCELLED);
                statusLabel.setText(appointment.getStatus().toString());
                UiUtils.showInformation("Success", "Appointment cancelled successfully.");
            } catch (Exception ex) {
                UiUtils.showError("Cancellation Failed", "Failed to cancel appointment", ex);
            }
        }
    }

    private void navigateToAppointmentList() {
        Stage stage = (Stage) appointmentIdLabel.getScene().getWindow();
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

    private void navigateToHomepage() {
        Stage stage = (Stage) appointmentIdLabel.getScene().getWindow();
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
}