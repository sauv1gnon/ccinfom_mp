package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.services.GeocodingService;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminBranchFormController {

    private ServiceRegistry services;
    private Branch existingBranch;
    private Branch resultBranch;
    private boolean isEditMode = false;

    private double selectedLat;
    private double selectedLon;
    private String selectedAddress;
    private BranchMarkerLayer markerLayer;

    @FXML
    private Label titleLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField nameField;

    @FXML
    private TextField contactField;

    @FXML
    private TextField capacityField;

    @FXML
    private TextField addressSearchField;

    @FXML
    private Label searchResultLabel;

    @FXML
    private VBox suggestionsContainer;

    @FXML
    private TextArea addressField;

    @FXML
    private TextField latField;

    @FXML
    private TextField lonField;

    @FXML
    private MapView mapView;

    @FXML
    private Button saveButton;

    @FXML
    private void initialize() {
        setupMap();
        latField.setEditable(false);
        lonField.setEditable(false);
    }

    public void setServices(ServiceRegistry services) {
        this.services = services;
    }

    public void setExistingBranch(Branch branch) {
        this.existingBranch = branch;
        this.isEditMode = true;
        titleLabel.setText("Edit Branch");
        populateFields();
    }

    public void setCreateMode() {
        this.isEditMode = false;
        titleLabel.setText("Add Branch");
    }

    public Branch getResultBranch() {
        return resultBranch;
    }

    private void setupMap() {
        mapView.setCenter(new MapPoint(14.5995, 120.9842));
        mapView.setZoom(11);
    }

    private void populateFields() {
        if (existingBranch == null) return;

        nameField.setText(existingBranch.getBranchName());
        contactField.setText(existingBranch.getContactNumber());
        capacityField.setText(String.valueOf(existingBranch.getCapacity()));
        addressField.setText(existingBranch.getAddress());

        if (existingBranch.getLatitude() != null && existingBranch.getLongitude() != null) {
            selectedLat = existingBranch.getLatitude();
            selectedLon = existingBranch.getLongitude();
            selectedAddress = existingBranch.getAddress();
            latField.setText(String.format("%.6f", selectedLat));
            lonField.setText(String.format("%.6f", selectedLon));
            updateMapMarker();
        }
    }

    @FXML
    private void handleSearchAddress() {
        String query = addressSearchField.getText().trim();
        if (query.isEmpty()) {
            updateStatus("Please enter an address to search");
            return;
        }

        boolean serviceAvailable = services.getGeocodingService().isServiceAvailable();
        
        if (!serviceAvailable) {
            searchResultLabel.setText("✗ Geocoding service unavailable");
            updateStatus("Backend services not running. Use manual coordinates.");
            showManualLocationDialog();
            return;
        }

        updateStatus("Searching for addresses...");
        searchResultLabel.setText("");
        suggestionsContainer.getChildren().clear();

        CompletableFuture.supplyAsync(() -> {
            try {
                return services.getGeocodingService().searchAddresses(query, 5);
            } catch (Exception e) {
                System.out.println("[AdminBranchForm] Search error: " + e.getMessage());
                return null;
            }
        }).thenAccept(results -> Platform.runLater(() -> {
            if (results != null && !results.isEmpty()) {
                searchResultLabel.setText("✓ Found " + results.size() + " results");
                updateStatus("Select an address from suggestions");
                displaySuggestions(results);
            } else {
                searchResultLabel.setText("✗ No addresses found");
                updateStatus("Try a different search or use manual input");
                showManualLocationDialog();
            }
        }));
    }

    private void displaySuggestions(List<GeocodingService.GeocodingResult> results) {
        suggestionsContainer.getChildren().clear();

        for (GeocodingService.GeocodingResult result : results) {
            Button suggestionBtn = new Button(result.getDisplayName());
            suggestionBtn.setMaxWidth(Double.MAX_VALUE);
            suggestionBtn.setWrapText(true);
            suggestionBtn.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 8; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");
            
            suggestionBtn.setOnMouseEntered(e -> 
                suggestionBtn.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 8; -fx-background-color: #e9ecef; -fx-border-color: #007bff; -fx-border-width: 1;")
            );
            suggestionBtn.setOnMouseExited(e -> 
                suggestionBtn.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 8; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;")
            );
            
            suggestionBtn.setOnAction(e -> selectAddress(result));
            
            VBox.setMargin(suggestionBtn, new Insets(0, 0, 4, 0));
            suggestionsContainer.getChildren().add(suggestionBtn);
        }
    }

    private void selectAddress(GeocodingService.GeocodingResult result) {
        selectedLat = result.getLatitude();
        selectedLon = result.getLongitude();
        selectedAddress = result.getDisplayName();

        addressField.setText(selectedAddress);
        latField.setText(String.format("%.6f", selectedLat));
        lonField.setText(String.format("%.6f", selectedLon));

        updateStatus("Address selected: " + selectedAddress);
        updateMapMarker();
    }

    @FXML
    private void handleManualLocation() {
        showManualLocationDialog();
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
                    selectedLat = Double.parseDouble(latStr);
                    selectedLon = Double.parseDouble(lonStr);
                    selectedAddress = "Manual Location (" + latStr + ", " + lonStr + ")";
                    
                    addressField.setText(selectedAddress);
                    latField.setText(String.format("%.6f", selectedLat));
                    lonField.setText(String.format("%.6f", selectedLon));
                    
                    updateStatus("Location set manually");
                    updateMapMarker();
                } catch (NumberFormatException e) {
                    updateStatus("Invalid coordinates");
                }
            });
        });
    }

    private void updateMapMarker() {
        if (markerLayer != null) {
            mapView.removeLayer(markerLayer);
        }
        
        markerLayer = new BranchMarkerLayer(selectedLat, selectedLon);
        mapView.addLayer(markerLayer);
        mapView.setCenter(new MapPoint(selectedLat, selectedLon));
        mapView.setZoom(14);
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String capacityStr = capacityField.getText().trim();

        if (name.isEmpty()) {
            updateStatus("Branch name is required");
            return;
        }

        if (selectedAddress == null || selectedAddress.isEmpty()) {
            updateStatus("Please select or enter an address");
            return;
        }

        int capacity = 0;
        try {
            if (!capacityStr.isEmpty()) {
                capacity = Integer.parseInt(capacityStr);
            }
        } catch (NumberFormatException e) {
            updateStatus("Invalid capacity value");
            return;
        }

        Branch branch = isEditMode ? existingBranch : new Branch();
        if (isEditMode) {
            branch.setBranchId(existingBranch.getBranchId());
        }
        
        branch.setBranchName(name);
        branch.setAddress(selectedAddress);
        branch.setContactNumber(contact);
        branch.setCapacity(capacity);
        branch.setLatitude(selectedLat);
        branch.setLongitude(selectedLon);

        resultBranch = branch;
        
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        resultBranch = null;
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private static class BranchMarkerLayer extends MapLayer {
        private final ObservableList<Pair<MapPoint, javafx.scene.Node>> points = FXCollections.observableArrayList();
        private final double lat;
        private final double lon;

        public BranchMarkerLayer(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
            addMarker();
        }

        private void addMarker() {
            MapPoint point = new MapPoint(lat, lon);
            
            Circle circle = new Circle(10);
            circle.setFill(Color.RED);
            circle.setStroke(Color.DARKRED);
            circle.setStrokeWidth(2);
            circle.setOpacity(0.8);
            
            points.add(new Pair<>(point, circle));
            this.getChildren().add(circle);
        }

        @Override
        protected void layoutLayer() {
            for (Pair<MapPoint, javafx.scene.Node> candidate : points) {
                MapPoint point = candidate.getKey();
                javafx.scene.Node node = candidate.getValue();
                Point2D mapPoint = getMapPoint(point.getLatitude(), point.getLongitude());
                node.setVisible(true);
                node.setTranslateX(mapPoint.getX());
                node.setTranslateY(mapPoint.getY());
            }
        }
    }
}
