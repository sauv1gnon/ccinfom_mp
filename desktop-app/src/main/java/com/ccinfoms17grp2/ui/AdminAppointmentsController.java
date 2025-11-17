package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.AppointmentStatus;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;

public class AdminAppointmentsController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;

    @FXML
    private TableView<Appointment> appointmentsTable;

    @FXML
    private TableColumn<Appointment, Integer> appointmentIdColumn;

    @FXML
    private TableColumn<Appointment, Integer> patientIdColumn;

    @FXML
    private TableColumn<Appointment, Integer> doctorIdColumn;

    @FXML
    private TableColumn<Appointment, Integer> branchIdColumn;

    @FXML
    private TableColumn<Appointment, LocalDateTime> datetimeColumn;

    @FXML
    private TableColumn<Appointment, AppointmentStatus> statusColumn;

    @FXML
    private TableColumn<Appointment, LocalDateTime> createdAtColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        doctorIdColumn.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        branchIdColumn.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        datetimeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentDateTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        datetimeColumn.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        createdAtColumn.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    @FXML
    private void handleConfirmAppointment() {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an appointment to confirm");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        try {
            services.getAppointmentService().updateAppointmentStatus(selected.getAppointmentId(), AppointmentStatus.IN_PROGRESS);
            statusLabel.setText("Appointment confirmed: #" + selected.getAppointmentId());
            loadAppointments();
        } catch (RuntimeException ex) {
            statusLabel.setText("Failed to confirm appointment");
            UiUtils.showError("Confirm Appointment Failed", ex.getMessage());
        }
    }

    @FXML
    private void handleCancelAppointment() {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an appointment to cancel");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        boolean confirm = UiUtils.showConfirmation(
            "Cancel Appointment",
            "Are you sure you want to cancel appointment #" + selected.getAppointmentId() + "?"
        );

        if (confirm) {
            try {
                services.getAppointmentService().cancelAppointment(selected.getAppointmentId());
                statusLabel.setText("Appointment cancelled: #" + selected.getAppointmentId());
                loadAppointments();
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to cancel appointment");
                UiUtils.showError("Cancel Appointment Failed", ex.getMessage());
            }
        }
    }

    @FXML
    private void handleSearch() {
        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        if (query.isEmpty()) {
            loadAppointments();
            return;
        }

        try {
            int appointmentId = Integer.parseInt(query);
            java.util.Optional<Appointment> appointmentOpt = services.getAppointmentService().getAppointmentById(appointmentId);
            if (appointmentOpt.isPresent()) {
                appointmentsTable.setItems(FXCollections.observableArrayList(appointmentOpt.get()));
                statusLabel.setText("Found appointment #" + appointmentId);
            } else {
                appointmentsTable.setItems(FXCollections.observableArrayList());
                statusLabel.setText("No appointment found with ID: " + appointmentId);
            }
        } catch (NumberFormatException ex) {
            statusLabel.setText("Invalid appointment ID");
        } catch (RuntimeException ex) {
            statusLabel.setText("Search failed");
            UiUtils.showError("Search Failed", ex.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadAppointments();
    }

    @FXML
    private void handleBackToDashboard() {
        navigator.show(UiView.ADMIN_DASHBOARD);
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
    public void onDisplay() {
        statusLabel.setText("");
        loadAppointments();
    }

    private void loadAppointments() {
        if (services == null || appointmentsTable == null) {
            return;
        }

        try {
            List<Appointment> appointments = services.getAppointmentService().listAppointments();
            appointmentsTable.setItems(FXCollections.observableArrayList(appointments));
            statusLabel.setText("Loaded " + appointments.size() + " appointments");
        } catch (RuntimeException ex) {
            statusLabel.setText("Failed to load appointments");
            UiUtils.showError("Load Appointments Failed", ex.getMessage());
        }
    }
}
