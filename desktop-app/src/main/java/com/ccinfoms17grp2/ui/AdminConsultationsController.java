package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Consultation;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;

public class AdminConsultationsController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;

    @FXML
    private TableView<Consultation> consultationsTable;

    @FXML
    private TableColumn<Consultation, Integer> consultationIdColumn;

    @FXML
    private TableColumn<Consultation, Integer> appointmentIdColumn;

    @FXML
    private TableColumn<Consultation, String> diagnosisColumn;

    @FXML
    private TableColumn<Consultation, String> treatmentPlanColumn;

    @FXML
    private TableColumn<Consultation, LocalDateTime> createdAtColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        consultationIdColumn.setCellValueFactory(new PropertyValueFactory<>("consultationId"));
        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        diagnosisColumn.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        treatmentPlanColumn.setCellValueFactory(new PropertyValueFactory<>("treatmentPlan"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));

        createdAtColumn.setCellFactory(column -> new TableCell<Consultation, LocalDateTime>() {
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
    private void handleViewDetails() {
        Consultation selected = consultationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a consultation to view details");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Consultation Details");
        alert.setHeaderText("Consultation #" + selected.getConsultationId());
        
        StringBuilder details = new StringBuilder();
        details.append("Appointment ID: ").append(selected.getAppointmentId()).append("\n");
        details.append("Start Time: ").append(selected.getStartTime()).append("\n");
        details.append("End Time: ").append(selected.getEndTime() != null ? selected.getEndTime() : "N/A").append("\n");
        details.append("Diagnosis: ").append(selected.getDiagnosis()).append("\n");
        details.append("Treatment Plan: ").append(selected.getTreatmentPlan()).append("\n");
        details.append("Prescription: ").append(selected.getPrescription() != null ? selected.getPrescription() : "N/A").append("\n");
        details.append("Follow-up Date: ").append(selected.getFollowUpDate() != null ? selected.getFollowUpDate() : "N/A");
        
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleSearch() {
        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        if (query.isEmpty()) {
            loadConsultations();
            return;
        }

        try {
            int consultationId = Integer.parseInt(query);
            java.util.Optional<Consultation> consultationOpt = services.getConsultationService().getConsultationById(consultationId);
            if (consultationOpt.isPresent()) {
                consultationsTable.setItems(FXCollections.observableArrayList(consultationOpt.get()));
                statusLabel.setText("Found consultation #" + consultationId);
            } else {
                consultationsTable.setItems(FXCollections.observableArrayList());
                statusLabel.setText("No consultation found with ID: " + consultationId);
            }
        } catch (NumberFormatException ex) {
            statusLabel.setText("Invalid consultation ID");
        } catch (RuntimeException ex) {
            statusLabel.setText("Search failed");
            UiUtils.showError("Search Failed", ex.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadConsultations();
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
        loadConsultations();
    }

    private void loadConsultations() {
        if (services == null || consultationsTable == null) {
            return;
        }

        try {
            List<Consultation> consultations = services.getConsultationService().listConsultations();
            consultationsTable.setItems(FXCollections.observableArrayList(consultations));
            statusLabel.setText("Loaded " + consultations.size() + " consultations");
        } catch (RuntimeException ex) {
            statusLabel.setText("Failed to load consultations");
            UiUtils.showError("Load Consultations Failed", ex.getMessage());
        }
    }
}
