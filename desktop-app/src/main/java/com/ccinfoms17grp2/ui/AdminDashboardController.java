package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.AppointmentStatus;
import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;
    private SessionContext session;

    @FXML
    private Label statusLabel;

    @FXML
    private void handleManageUsers() {
        navigator.show(UiView.ADMIN_USERS);
    }

    @FXML
    private void handleManagePatients() {
        navigator.show(UiView.ADMIN_PATIENTS);
    }

    @FXML
    private void handleManageDoctors() {
        navigator.show(UiView.ADMIN_DOCTORS);
    }

    @FXML
    private void handleManageSpecializations() {
        navigator.show(UiView.ADMIN_SPECIALIZATIONS);
    }

    @FXML
    private void handleConfirmPatientAppointment() {
        if (!ensureServices()) {
            return;
        }
        promptAppointmentId("Confirm Appointment").ifPresent(appointmentId -> {
            try {
                services.getAppointmentService().updateAppointmentStatus(appointmentId, AppointmentStatus.IN_PROGRESS);
                updateStatus("Appointment #" + appointmentId + " confirmed");
            } catch (RuntimeException ex) {
                reportError("Unable to confirm appointment", ex);
            }
        });
    }

    @FXML
    private void handleCancelPatientAppointment() {
        if (!ensureServices()) {
            return;
        }
        promptAppointmentId("Cancel Appointment").ifPresent(appointmentId -> {
            try {
                services.getAppointmentService().cancelAppointment(appointmentId);
                updateStatus("Appointment #" + appointmentId + " canceled");
            } catch (RuntimeException ex) {
                reportError("Unable to cancel appointment", ex);
            }
        });
    }

    @FXML
    private void handleAssignDoctors() {
        if (!ensureServices()) {
            return;
        }
        promptBranch("Select branch for assignments").ifPresent(branch -> {
            try {
                int count = services.getDoctorService().getDoctorsByBranch(branch.getBranchId()).size();
                updateStatus("Branch " + branch.getBranchName() + " has " + count + " assigned doctors");
            } catch (RuntimeException ex) {
                reportError("Unable to load doctor assignments", ex);
            }
        });
    }

    @FXML
    private void handleManageQueues() {
        navigator.show(UiView.ADMIN_QUEUES);
    }

    @FXML
    private void handleManageBranchAppointments() {
        navigator.show(UiView.ADMIN_BRANCHES);
    }

    @FXML
    private void handleManageAppointments() {
        navigator.show(UiView.ADMIN_APPOINTMENTS);
    }

    @FXML
    private void handleManageConsultations() {
        navigator.show(UiView.ADMIN_CONSULTATIONS);
    }

    @FXML
    private void handleMonitorQueues() {
        navigator.show(UiView.ADMIN_QUEUES);
    }

    @FXML
    private void handleSignOut() {
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
        if (session == null || !session.isUserType(User.UserType.ADMIN)) {
            statusLabel.setText("Admin access required");
            return;
        }
        statusLabel.setText("Overview of operations");
    }

    private boolean ensureServices() {
        if (services == null) {
            updateStatus("Service registry unavailable");
            return false;
        }
        return true;
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void reportError(String action, RuntimeException ex) {
        updateStatus(action);
        UiUtils.showError(action, ex.getMessage());
    }

    private Optional<Integer> promptAppointmentId(String title) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(title + " - enter appointment ID");
        dialog.setContentText("Appointment ID:");
        Optional<String> value = dialog.showAndWait();
        if (!value.isPresent()) {
            return Optional.empty();
        }
        String input = value.get().trim();
        if (input.isEmpty()) {
            return Optional.empty();
        }
        try {
            int appointmentId = Integer.parseInt(input);
            return Optional.of(appointmentId);
        } catch (NumberFormatException ex) {
            updateStatus("Invalid appointment ID");
            return Optional.empty();
        }
    }

    private Optional<Branch> promptBranch(String header) {
        List<Branch> branches = services.getBranchService().listBranches();
        if (branches.isEmpty()) {
            UiUtils.showWarning("Branches", "No branches configured");
            return Optional.empty();
        }
        List<String> options = new ArrayList<>();
        for (Branch branch : branches) {
            options.add(branch.getBranchId() + ": " + branch.getBranchName());
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
        dialog.setTitle("Select Branch");
        dialog.setHeaderText(header);
        dialog.setContentText("Branch:");
        Optional<String> selection = dialog.showAndWait();
        if (!selection.isPresent()) {
            return Optional.empty();
        }
        String choice = selection.get();
        int separatorIndex = choice.indexOf(':');
        if (separatorIndex <= 0) {
            return Optional.empty();
        }
        try {
            int branchId = Integer.parseInt(choice.substring(0, separatorIndex).trim());
            return branches.stream().filter(branch -> branch.getBranchId() == branchId).findFirst();
        } catch (NumberFormatException ex) {
            updateStatus("Invalid branch selection");
            return Optional.empty();
        }
    }
}
