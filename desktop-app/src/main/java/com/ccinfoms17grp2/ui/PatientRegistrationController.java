package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PatientRegistrationController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField contactField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField confirmPasswordField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private void handleSubmit() {
        if (services == null) {
            feedbackLabel.setText("Services unavailable");
            return;
        }
        String firstName = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
        String lastName = lastNameField.getText() == null ? "" : lastNameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String contact = contactField.getText() == null ? "" : contactField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText().trim();
        
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || contact.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            feedbackLabel.setText("Complete all required fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            feedbackLabel.setText("Passwords do not match");
            return;
        }
        
        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setEmail(email);
        patient.setContactNumber(contact);
        
        try {
            Patient created = services.getPatientService().createPatient(patient);
            services.getAuthService().registerPatient(email, password, created.getPatientId());
            feedbackLabel.setText("Registration successful! Patient #" + created.getPatientId() + " created.");
            clearForm();
        } catch (ValidationException ex) {
            feedbackLabel.setText("Validation error: " + ex.getMessage());
        } catch (RuntimeException ex) {
            feedbackLabel.setText("Registration failed: " + ex.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
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
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        contactField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}
