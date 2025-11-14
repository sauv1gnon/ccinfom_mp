package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.*;
import com.ccinfoms17grp2.services.*;
import com.ccinfoms17grp2.utils.DateTimeUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppointmentListController implements Initializable {

    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> appointmentIdColumn;
    @FXML private TableColumn<Appointment, String> appointmentDateColumn;
    @FXML private TableColumn<Appointment, String> doctorNameColumn;
    @FXML private TableColumn<Appointment, String> branchNameColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;

    private final User currentUser;
    private final ServiceRegistry services;
    private final ExecutorService executorService;
    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();

    public AppointmentListController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        appointmentTable.setItems(appointments);
        appointmentTable.setOnMouseClicked(this::handleTableClick);
        loadAppointments();
    }

    private void setupTableColumns() {
        appointmentIdColumn.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getAppointmentId())));
        
        appointmentDateColumn.setCellValueFactory(data ->
            new SimpleStringProperty(DateTimeUtil.format(data.getValue().getAppointmentDateTime())));
        
        doctorNameColumn.setCellValueFactory(data -> {
            Optional<Doctor> doctor = findDoctorById(data.getValue().getDoctorId());
            if (doctor.isPresent()) {
                return new SimpleStringProperty(doctor.get().getFirstName() + " " + doctor.get().getLastName());
            }
            return new SimpleStringProperty("Doctor ID: " + data.getValue().getDoctorId());
        });
        
        branchNameColumn.setCellValueFactory(data -> {
            Optional<Branch> branch = findBranchById(data.getValue().getBranchId());
            if (branch.isPresent()) {
                return new SimpleStringProperty(branch.get().getBranchName());
            }
            return new SimpleStringProperty("Branch ID: " + data.getValue().getBranchId());
        });
        
        statusColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatus().toString()));
    }

    private void loadAppointments() {
        Task<List<Appointment>> loadTask = new Task<>() {
            @Override
            protected List<Appointment> call() {
                return new ArrayList<>();
            }
        };

        loadTask.setOnSucceeded(event -> {
            appointments.clear();
            appointments.addAll(loadTask.getValue());
        });

        loadTask.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load appointments", event.getSource().getException());
        });

        executorService.execute(loadTask);
    }

    private Optional<Doctor> findDoctorById(int doctorId) {
        try {
            return services.getDoctorService().listDoctors().stream()
                .filter(d -> d.getDoctorId() == doctorId)
                .findFirst();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Optional<Branch> findBranchById(int branchId) {
        try {
            return services.getBranchService().listBranches().stream()
                .filter(b -> b.getBranchId() == branchId)
                .findFirst();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 2 && appointmentTable.getSelectionModel().getSelectedItem() != null) {
            Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
            navigateToAppointmentDetails(selectedAppointment);
        }
    }

    @FXML
    private void handleBackToHomepage() {
        navigateToHomepage();
    }

    @FXML
    private void handleViewConsultations() {
        navigateToConsultationList();
    }

    @FXML
    private void handleBookAppointment() {
        navigateToBookAppointment();
    }

    private void navigateToHomepage() {
        Stage stage = (Stage) appointmentTable.getScene().getWindow();
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

    private void navigateToConsultationList() {
        Stage stage = (Stage) appointmentTable.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/consultation-list.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == ConsultationListController.class) {
                    return new ConsultationListController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to consultation list", ex);
        }
    }

    private void navigateToBookAppointment() {
        Stage stage = (Stage) appointmentTable.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/book-appointment.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == BookAppointmentController.class) {
                    return new BookAppointmentController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to book appointment", ex);
        }
    }

    private void navigateToAppointmentDetails(Appointment appointment) {
        Stage stage = (Stage) appointmentTable.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/appointment-details.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AppointmentDetailsController.class) {
                    return new AppointmentDetailsController(services, currentUser, appointment);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to appointment details", ex);
        }
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}