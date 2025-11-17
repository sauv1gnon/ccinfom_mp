package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.AppointmentStatus;
import com.ccinfoms17grp2.models.BranchWithDoctors;
import com.ccinfoms17grp2.models.BranchWithDoctors.DoctorAvailabilityInfo;
import com.ccinfoms17grp2.models.Specialization;
import com.ccinfoms17grp2.services.GeocodingService;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AppointmentBookingDialogController {

    private ServiceRegistry services;
    private int patientId;
    private Appointment createdAppointment;

    private double patientLat;
    private double patientLon;
    private String patientLocationName;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField locationField;

    @FXML
    private Label locationResultLabel;

    @FXML
    private ComboBox<Specialization> specializationCombo;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> timeCombo;

    @FXML
    private VBox branchListContainer;

    @FXML
    private WebView mapView;

    private WebEngine webEngine;

    @FXML
    private void initialize() {
        setupTimeComboBox();
        setupMap();
    }

    public void setServices(ServiceRegistry services) {
        this.services = services;
        loadSpecializations();
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public Appointment getCreatedAppointment() {
        return createdAppointment;
    }

    private void setupTimeComboBox() {
        List<String> times = new ArrayList<>();
        for (int hour = 8; hour <= 17; hour++) {
            times.add(String.format("%02d:00", hour));
            times.add(String.format("%02d:30", hour));
        }
        timeCombo.getItems().addAll(times);
        timeCombo.setValue("09:00");
    }

    private void setupMap() {
        webEngine = mapView.getEngine();
        String mapHtml = generateMapHtml();
        webEngine.loadContent(mapHtml);
    }

    private void loadSpecializations() {
        List<Specialization> specializations = services.getSpecializationService().listSpecializations();
        specializationCombo.getItems().addAll(specializations);
        specializationCombo.setConverter(new javafx.util.StringConverter<Specialization>() {
            @Override
            public String toString(Specialization spec) {
                return spec != null ? spec.getSpecializationName() : "";
            }

            @Override
            public Specialization fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    private void handleSearchLocation() {
        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            updateStatus("Please enter a location");
            return;
        }

        if (!services.getGeocodingService().isServiceAvailable()) {
            locationResultLabel.setText("✗ Geocoding service unavailable");
            updateStatus("Backend services not running. Using manual coordinates.");
            showManualLocationDialog();
            return;
        }

        updateStatus("Searching for location...");
        locationResultLabel.setText("");

        CompletableFuture.supplyAsync(() -> {
            try {
                return services.getGeocodingService().geocodeAddress(location);
            } catch (Exception e) {
                return null;
            }
        }).thenAccept(result -> Platform.runLater(() -> {
            if (result != null) {
                patientLat = result.getLatitude();
                patientLon = result.getLongitude();
                patientLocationName = result.getDisplayName();
                locationResultLabel.setText("✓ Found: " + patientLocationName);
                updateStatus("Location found");
                updateMapWithPatientLocation();
            } else {
                locationResultLabel.setText("✗ Location not found");
                updateStatus("Location not found. Try a different search or use manual input.");
                showManualLocationDialog();
            }
        }));
    }

    private void showManualLocationDialog() {
        TextInputDialog latDialog = new TextInputDialog("14.5995");
        latDialog.setTitle("Manual Location");
        latDialog.setHeaderText("Enter Coordinates");
        latDialog.setContentText("Latitude:");
        
        latDialog.showAndWait().ifPresent(latStr -> {
            TextInputDialog lonDialog = new TextInputDialog("120.9842");
            lonDialog.setTitle("Manual Location");
            lonDialog.setHeaderText("Enter Coordinates");
            lonDialog.setContentText("Longitude:");
            
            lonDialog.showAndWait().ifPresent(lonStr -> {
                try {
                    patientLat = Double.parseDouble(latStr);
                    patientLon = Double.parseDouble(lonStr);
                    patientLocationName = "Manual Location (" + latStr + ", " + lonStr + ")";
                    locationResultLabel.setText("✓ " + patientLocationName);
                    updateStatus("Location set manually");
                    updateMapWithPatientLocation();
                } catch (NumberFormatException e) {
                    updateStatus("Invalid coordinates");
                }
            });
        });
    }

    @FXML
    private void handleLoadBranches() {
        if (patientLocationName == null) {
            updateStatus("Please search for your location first");
            return;
        }

        Specialization selectedSpec = specializationCombo.getValue();
        if (selectedSpec == null) {
            updateStatus("Please select a specialization");
            return;
        }

        LocalDate date = datePicker.getValue();
        String time = timeCombo.getValue();
        if (date == null || time == null) {
            updateStatus("Please select date and time");
            return;
        }

        LocalDateTime preferredSchedule = LocalDateTime.of(date, LocalTime.parse(time));

        updateStatus("Loading nearby branches...");
        branchListContainer.getChildren().clear();

        CompletableFuture.supplyAsync(() -> {
            return services.getEnhancedBranchSearchService().searchBranches(
                patientLat, patientLon, selectedSpec.getSpecializationId(), 
                preferredSchedule, 7
            );
        }).thenAccept(branches -> Platform.runLater(() -> {
            if (branches.isEmpty()) {
                updateStatus("No branches found with matching doctors");
                Label emptyLabel = new Label("No branches available");
                emptyLabel.setStyle("-fx-text-fill: #6c757d;");
                branchListContainer.getChildren().add(emptyLabel);
            } else {
                updateStatus("Found " + branches.size() + " nearby branches");
                displayBranches(branches, preferredSchedule);
                updateMapWithBranches(branches);
            }
        }));
    }

    private void displayBranches(List<BranchWithDoctors> branches, LocalDateTime schedule) {
        branchListContainer.getChildren().clear();

        for (BranchWithDoctors branchData : branches) {
            VBox branchCard = createBranchCard(branchData, schedule);
            branchListContainer.getChildren().add(branchCard);
        }
    }

    private VBox createBranchCard(BranchWithDoctors branchData, LocalDateTime schedule) {
        VBox card = new VBox(6.0);
        card.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 10;");

        Label branchName = new Label(branchData.getBranch().getBranchName());
        branchName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label distance = new Label(String.format("%.1f km away", branchData.getDistanceKm()));
        distance.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

        Label address = new Label(branchData.getBranch().getAddress());
        address.setStyle("-fx-text-fill: #495057; -fx-font-size: 11px;");
        address.setWrapText(true);

        card.getChildren().addAll(branchName, distance, address);

        VBox doctorsBox = new VBox(4.0);
        doctorsBox.setPadding(new Insets(8, 0, 0, 0));

        for (DoctorAvailabilityInfo doctorInfo : branchData.getDoctors()) {
            HBox doctorRow = new HBox(8.0);
            doctorRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label colorIndicator = new Label("●");
            colorIndicator.setStyle("-fx-font-size: 16px; -fx-text-fill: " + getColorHex(doctorInfo.getColor()) + ";");

            Label doctorName = new Label(doctorInfo.getDisplayName());
            doctorName.setStyle("-fx-font-size: 12px;");

            Button selectBtn = new Button("Select");
            selectBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 10 3 10;");
            
            if (doctorInfo.getColor() == BranchWithDoctors.AvailabilityColor.GREEN) {
                selectBtn.setStyle(selectBtn.getStyle() + " -fx-background-color: #28a745; -fx-text-fill: white;");
            } else {
                selectBtn.setStyle(selectBtn.getStyle() + " -fx-background-color: #6c757d; -fx-text-fill: white;");
            }

            selectBtn.setOnAction(e -> handleSelectDoctor(
                branchData.getBranch().getBranchId(), 
                doctorInfo.getDoctor().getDoctorId(), 
                schedule
            ));

            doctorRow.getChildren().addAll(colorIndicator, doctorName, selectBtn);
            HBox.setHgrow(doctorName, javafx.scene.layout.Priority.ALWAYS);

            doctorsBox.getChildren().add(doctorRow);
        }

        card.getChildren().add(doctorsBox);

        return card;
    }

    private String getColorHex(BranchWithDoctors.AvailabilityColor color) {
        switch (color) {
            case GREEN: return "#28a745";
            case YELLOW: return "#ffc107";
            case RED: return "#dc3545";
            default: return "#6c757d";
        }
    }

    private void handleSelectDoctor(int branchId, int doctorId, LocalDateTime schedule) {
        try {
            Appointment appointment = new Appointment();
            appointment.setPatientId(patientId);
            appointment.setDoctorId(doctorId);
            appointment.setBranchId(branchId);
            appointment.setAppointmentDateTime(schedule);
            appointment.setStatus(AppointmentStatus.SCHEDULED);

            createdAppointment = services.getAppointmentService().createAppointment(appointment);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Appointment Created");
            alert.setContentText("Your appointment has been scheduled successfully!");
            alert.showAndWait();

            Stage stage = (Stage) branchListContainer.getScene().getWindow();
            stage.close();
        } catch (Exception ex) {
            UiUtils.showError("Create Appointment", "Failed to create appointment: " + ex.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) branchListContainer.getScene().getWindow();
        stage.close();
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private String generateMapHtml() {
        return "<!DOCTYPE html>" +
                "<html><head>" +
                "<meta charset='utf-8'>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>body,html,#map{margin:0;padding:0;height:100%;width:100%;}</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "var map = L.map('map').setView([14.5995, 120.9842], 11);" +
                "L.tileLayer('http://localhost:8082/styles/basic/{z}/{x}/{y}.png', {" +
                "  maxZoom: 18" +
                "}).addTo(map);" +
                "</script>" +
                "</body></html>";
    }

    private void updateMapWithPatientLocation() {
        String script = String.format(
            "if (window.patientMarker) { map.removeLayer(window.patientMarker); }" +
            "window.patientMarker = L.marker([%f, %f], {" +
            "  icon: L.icon({iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png', iconSize: [25, 41], iconAnchor: [12, 41]})" +
            "}).addTo(map).bindPopup('Your Location');" +
            "map.setView([%f, %f], 13);",
            patientLat, patientLon, patientLat, patientLon
        );
        webEngine.executeScript(script);
    }

    private void updateMapWithBranches(List<BranchWithDoctors> branches) {
        StringBuilder script = new StringBuilder();
        script.append("if (window.branchMarkers) { window.branchMarkers.forEach(m => map.removeLayer(m)); }");
        script.append("window.branchMarkers = [];");

        for (BranchWithDoctors branch : branches) {
            script.append(String.format(
                "var marker = L.marker([%f, %f], {" +
                "  icon: L.icon({iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png', iconSize: [25, 41], iconAnchor: [12, 41]})" +
                "}).addTo(map).bindPopup('%s<br>%.1f km');" +
                "window.branchMarkers.push(marker);",
                branch.getBranch().getLatitude(),
                branch.getBranch().getLongitude(),
                branch.getBranch().getBranchName().replace("'", "\\'"),
                branch.getDistanceKm()
            ));
        }

        webEngine.executeScript(script.toString());
    }
}
