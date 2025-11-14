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
import java.util.ResourceBundle;

public class ConsultationDetailsController implements Initializable {

    @FXML private Label consultationIdLabel;
    @FXML private Label appointmentIdLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label endTimeLabel;
    @FXML private Label diagnosisLabel;
    @FXML private Label treatmentPlanLabel;
    @FXML private Label prescriptionLabel;
    @FXML private Label followUpDateLabel;

    private final User currentUser;
    private final ServiceRegistry services;
    private final Consultation consultation;

    public ConsultationDetailsController(ServiceRegistry services, User currentUser, Consultation consultation) {
        this.services = services;
        this.currentUser = currentUser;
        this.consultation = consultation;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadConsultationDetails();
    }

    private void loadConsultationDetails() {
        consultationIdLabel.setText(String.valueOf(consultation.getConsultationId()));
        appointmentIdLabel.setText(String.valueOf(consultation.getAppointmentId()));
        startTimeLabel.setText(DateTimeUtil.format(consultation.getStartTime()));
        endTimeLabel.setText(DateTimeUtil.format(consultation.getEndTime()));
        diagnosisLabel.setText(consultation.getDiagnosis() != null ? consultation.getDiagnosis() : "N/A");
        treatmentPlanLabel.setText(consultation.getTreatmentPlan() != null ? consultation.getTreatmentPlan() : "N/A");
        prescriptionLabel.setText(consultation.getPrescription() != null ? consultation.getPrescription() : "N/A");
        followUpDateLabel.setText(consultation.getFollowUpDate() != null ? 
            DateTimeUtil.format(consultation.getFollowUpDate()) : "N/A");
    }

    @FXML
    private void handleBackToConsultations() {
        navigateToConsultationList();
    }

    @FXML
    private void handleBackToHomepage() {
        navigateToHomepage();
    }

    private void navigateToConsultationList() {
        Stage stage = (Stage) consultationIdLabel.getScene().getWindow();
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

    private void navigateToHomepage() {
        Stage stage = (Stage) consultationIdLabel.getScene().getWindow();
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