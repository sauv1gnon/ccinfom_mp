package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.AuthenticationException;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;
    private SessionContext session;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    private void handlePatientLogin() {
        authenticate(User.UserType.PATIENT, UiView.PATIENT_HOME, "Signed in as patient");
    }

    @FXML
    private void handleDoctorLogin() {
        authenticate(User.UserType.DOCTOR, UiView.DOCTOR_DASHBOARD, "Signed in as doctor");
    }

    @FXML
    private void handleAdminLogin() {
        authenticate(User.UserType.ADMIN, UiView.ADMIN_DASHBOARD, "Signed in as admin");
    }

    @FXML
    private void handleOpenRegistration() {
        navigator.show(UiView.PATIENT_REGISTRATION);
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
        usernameField.clear();
        passwordField.clear();
        statusLabel.setText("Sign in to continue");
        if (session != null) {
            session.clear();
        }
    }

    private void authenticate(User.UserType requiredType, UiView destination, String successMessage) {
        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }
        String email = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Email and password are required");
            return;
        }
        try {
            User user = services.getAuthService().authenticate(email, password);
            if (user.getUserType() != requiredType) {
                statusLabel.setText("Account is not registered as " + requiredType.name().toLowerCase());
                return;
            }
            if (session != null) {
                session.setCurrentUser(user);
            }
            statusLabel.setText(successMessage);
            navigator.show(destination);
        } catch (AuthenticationException ex) {
            statusLabel.setText(ex.getMessage());
        } catch (RuntimeException ex) {
            statusLabel.setText("Unable to sign in");
            UiUtils.showError("Login failed", ex.getMessage());
        }
    }
}
