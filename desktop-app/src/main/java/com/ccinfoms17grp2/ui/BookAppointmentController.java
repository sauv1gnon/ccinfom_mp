package com.ccinfoms17grp2.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.ccinfoms17grp2.models.*;
import com.ccinfoms17grp2.services.*;
import com.ccinfoms17grp2.services.BranchRecommendationService.BranchRecommendation;
import com.ccinfoms17grp2.services.GeocodingService.GeocodingResult;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BookAppointmentController implements Initializable {

    // FXML Controls - Step indicators
    @FXML private Label step1Icon, step2Icon, step3Icon, step4Icon, step5Icon;
    
    // FXML Controls - Navigation
    @FXML private Button cancelButton, bookButton, geocodeButton, nextStep2Button, nextStep4Button;
    
    // FXML Controls - Step containers
    @FXML private StackPane stepContainer, progressOverlay;
    @FXML private javafx.scene.layout.VBox step1Panel, step2Panel, step3Panel, step4Panel, step5Panel;
    
    // FXML Controls - Progress indicator
    @FXML private Label progressLabel;
    
    // FXML Controls - Step 1 (Date/Time)
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> hourComboBox, minuteComboBox;
    
    // FXML Controls - Step 2 (Location)
    @FXML private TextField locationField;
    @FXML private Label geocodeResultLabel;
    
    // FXML Controls - Step 3 (Specializations)
    @FXML private ListView<CheckBox> specializationListView;
    
    // FXML Controls - Step 4 (Branch)
    @FXML private TableView<BranchRecommendation> branchTableView;
    @FXML private TableColumn<BranchRecommendation, String> branchNameColumn, branchAddressColumn;
    @FXML private TableColumn<BranchRecommendation, Double> branchDistanceColumn;
    @FXML private TableColumn<BranchRecommendation, Integer> branchDoctorCountColumn;
    @FXML private Label recommendationLabel;
    
    // FXML Controls - Step 5 (Doctor)
    @FXML private ListView<Doctor> doctorListView;
    @FXML private Label branchInfoLabel, summaryLabel;
    
    // State management
    private final User currentUser;
    private final ServiceRegistry services;
    private final GeocodingService geocodingService;
    private final BranchRecommendationService recommendationService;
    private int currentStep = 1;
    private LocalDateTime selectedDateTime;
    private Double patientLatitude, patientLongitude;
    private List<Integer> selectedSpecializationIds = new ArrayList<>();
    private BranchRecommendation selectedBranch;
    private Doctor selectedDoctor;

    public BookAppointmentController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.geocodingService = new GeocodingService();
        this.recommendationService = new BranchRecommendationService(
            new com.ccinfoms17grp2.dao.impl.BranchJdbcDao(),
            new com.ccinfoms17grp2.dao.impl.DoctorJdbcDao()
        );
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeStep1();
        initializeStep3();
        initializeStep4();
        initializeStep5();
        updateStepIndicators();
    }

    private void initializeStep1() {
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 8; i <= 20; i++) {
            hours.add(String.format("%02d", i));
        }
        hourComboBox.setItems(hours);
        
        minuteComboBox.setItems(FXCollections.observableArrayList("00", "30"));
    }

    private void initializeStep3() {
        try {
            List<Specialization> specializations = services.getSpecializationService().listSpecializations();
            ObservableList<CheckBox> checkBoxes = FXCollections.observableArrayList();
            for (Specialization spec : specializations) {
                CheckBox cb = new CheckBox(spec.getSpecializationName());
                cb.setUserData(spec.getSpecializationId());
                checkBoxes.add(cb);
            }
            specializationListView.setItems(checkBoxes);
        } catch (Exception e) {
            UiUtils.showError("Error", "Failed to load specializations", e);
        }
    }

    private void initializeStep4() {
        branchNameColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBranch().getBranchName()));
        branchAddressColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBranch().getAddress()));
        branchDistanceColumn.setCellValueFactory(new PropertyValueFactory<>("distanceKm"));
        branchDoctorCountColumn.setCellValueFactory(new PropertyValueFactory<>("availableDoctorCount"));
        
        branchTableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            nextStep4Button.setDisable(newVal == null);
        });
    }

    private void initializeStep5() {
        doctorListView.setCellFactory(lv -> new ListCell<Doctor>() {
            @Override
            protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) {
                    setText(null);
                } else {
                    setText(String.format("Dr. %s %s", doctor.getFirstName(), doctor.getLastName()));
                }
            }
        });
        
        doctorListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedDoctor = newVal;
                updateSummary();
                bookButton.setDisable(false);
            }
        });
    }

    private void updateStepIndicators() {
        step1Icon.setText(currentStep >= 1 ? "●" : "○");
        step2Icon.setText(currentStep >= 2 ? "●" : "○");
        step3Icon.setText(currentStep >= 3 ? "●" : "○");
        step4Icon.setText(currentStep >= 4 ? "●" : "○");
        step5Icon.setText(currentStep >= 5 ? "●" : "○");
    }

    @FXML
    private void handleNextStep() {
        if (validateCurrentStep()) {
            currentStep++;
            showStep(currentStep);
            updateStepIndicators();
        }
    }

    @FXML
    private void handlePreviousStep() {
        if (currentStep > 1) {
            currentStep--;
            showStep(currentStep);
            updateStepIndicators();
        }
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1:
                if (datePicker.getValue() == null || hourComboBox.getValue() == null || minuteComboBox.getValue() == null) {
                    UiUtils.showWarning("Validation Error", "Please select both date and time");
                    return false;
                }
                LocalDate selectedDate = datePicker.getValue();
                int hour = Integer.parseInt(hourComboBox.getValue());
                int minute = Integer.parseInt(minuteComboBox.getValue());
                selectedDateTime = LocalDateTime.of(selectedDate, LocalTime.of(hour, minute));
                
                if (selectedDateTime.isBefore(LocalDateTime.now())) {
                    UiUtils.showWarning("Validation Error", "Appointment must be in the future");
                    return false;
                }
                return true;
                
            case 2:
                if (patientLatitude == null || patientLongitude == null) {
                    UiUtils.showWarning("Validation Error", "Please verify your location first");
                    return false;
                }
                return true;
                
            case 3:
                selectedSpecializationIds = specializationListView.getItems().stream()
                    .filter(CheckBox::isSelected)
                    .map(cb -> (Integer) cb.getUserData())
                    .collect(Collectors.toList());
                
                if (selectedSpecializationIds.isEmpty()) {
                    UiUtils.showWarning("Validation Error", "Please select at least one specialization");
                    return false;
                }
                loadBranchRecommendations();
                return true;
                
            case 4:
                selectedBranch = branchTableView.getSelectionModel().getSelectedItem();
                if (selectedBranch == null) {
                    UiUtils.showWarning("Validation Error", "Please select a branch");
                    return false;
                }
                loadAvailableDoctors();
                return true;
                
            default:
                return true;
        }
    }

    private void showStep(int step) {
        step1Panel.setVisible(step == 1);
        step2Panel.setVisible(step == 2);
        step3Panel.setVisible(step == 3);
        step4Panel.setVisible(step == 4);
        step5Panel.setVisible(step == 5);
    }

    @FXML
    private void handleGeocodeLocation() {
        String address = locationField.getText();
        if (address == null || address.trim().isEmpty()) {
            UiUtils.showWarning("Validation Error", "Please enter an address");
            return;
        }
        
        showProgress("Geocoding address...");
        new Thread(() -> {
            try {
                GeocodingResult result = geocodingService.geocodeAddress(address);
                Platform.runLater(() -> {
                    hideProgress();
                    if (result != null) {
                        patientLatitude = result.getLatitude();
                        patientLongitude = result.getLongitude();
                        geocodeResultLabel.setText("✓ Location verified: " + result.getDisplayName());
                        geocodeResultLabel.setStyle("-fx-text-fill: green;");
                        nextStep2Button.setDisable(false);
                    } else {
                        UiUtils.showWarning("Geocoding Failed", "Could not find location. Please try a different address.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideProgress();
                    UiUtils.showError("Geocoding Error", "Failed to geocode address: " + e.getMessage(), e);
                });
            }
        }).start();
    }

    private void loadBranchRecommendations() {
        showProgress("Finding branches...");
        new Thread(() -> {
            try {
                List<BranchRecommendation> recommendations = recommendationService.recommendBranches(
                    patientLatitude, patientLongitude, selectedSpecializationIds);
                
                Platform.runLater(() -> {
                    hideProgress();
                    if (recommendations.isEmpty()) {
                        recommendationLabel.setText("No branches found with matching specializations");
                        branchTableView.setItems(FXCollections.observableArrayList());
                    } else {
                        recommendationLabel.setText(String.format("Found %d branches", recommendations.size()));
                        branchTableView.setItems(FXCollections.observableArrayList(recommendations));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideProgress();
                    UiUtils.showError("Error", "Failed to load branch recommendations", e);
                });
            }
        }).start();
    }

    private void loadAvailableDoctors() {
        showProgress("Loading doctors...");
        new Thread(() -> {
            try {
                List<Doctor> doctors = recommendationService.getAvailableDoctorsAtBranch(
                    selectedBranch.getBranch().getBranchId(), selectedSpecializationIds);
                
                Platform.runLater(() -> {
                    hideProgress();
                    if (doctors.isEmpty()) {
                        branchInfoLabel.setText("No available doctors at this branch");
                        doctorListView.setItems(FXCollections.observableArrayList());
                    } else {
                        branchInfoLabel.setText(String.format("%d doctors available at %s", 
                            doctors.size(), selectedBranch.getBranch().getBranchName()));
                        doctorListView.setItems(FXCollections.observableArrayList(doctors));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideProgress();
                    UiUtils.showError("Error", "Failed to load doctors", e);
                });
            }
        }).start();
    }

    private void updateSummary() {
        if (selectedDoctor != null && selectedBranch != null && selectedDateTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' h:mm a");
            String summary = String.format(
                "Date & Time: %s\n" +
                "Branch: %s\n" +
                "Doctor: Dr. %s %s\n" +
                "Distance: %.2f km from your location",
                selectedDateTime.format(formatter),
                selectedBranch.getBranch().getBranchName(),
                selectedDoctor.getFirstName(),
                selectedDoctor.getLastName(),
                selectedBranch.getDistanceKm()
            );
            summaryLabel.setText(summary);
        }
    }

    @FXML
    private void handleBookAppointment() {
        if (selectedDoctor == null || selectedBranch == null || selectedDateTime == null) {
            UiUtils.showWarning("Validation Error", "Please complete all steps");
            return;
        }
        
        showProgress("Booking appointment...");
        new Thread(() -> {
            try {
                Appointment appointment = new Appointment();
                appointment.setPatientId(currentUser.getPersonId());
                appointment.setDoctorId(selectedDoctor.getDoctorId());
                appointment.setBranchId(selectedBranch.getBranch().getBranchId());
                appointment.setAppointmentDateTime(selectedDateTime);
                appointment.setStatus(AppointmentStatus.SCHEDULED);
                
                Appointment created = services.getAppointmentService().createAppointment(appointment);
                
                Platform.runLater(() -> {
                    hideProgress();
                    navigateToAppointmentConfirmed(created);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideProgress();
                    UiUtils.showError("Booking Failed", "Failed to book appointment: " + e.getMessage(), e);
                });
            }
        }).start();
    }

    private void showProgress(String message) {
        progressLabel.setText(message);
        progressOverlay.setVisible(true);
        progressOverlay.setManaged(true);
    }

    private void hideProgress() {
        progressOverlay.setVisible(false);
        progressOverlay.setManaged(false);
    }

    @FXML
    private void handleCancelBooking() {
        navigateToAppointmentList();
    }

    @FXML
    private void handleBackToHomepage() {
        navigateToHomepage();
    }

    private void navigateToAppointmentList() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/appointment-list.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AppointmentListController.class) {
                    return new AppointmentListController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to appointment list", ex);
        }
    }

    private void navigateToAppointmentConfirmed(Appointment appointment) {
        Stage stage = (Stage) bookButton.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/appointment-confirmed.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AppointmentConfirmedController.class) {
                    return new AppointmentConfirmedController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to appointment confirmed", ex);
        }
    }

    private void navigateToHomepage() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/homepage.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == HomepageController.class) {
                    return new HomepageController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to homepage", ex);
        }
    }
}