package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.AuthenticationException;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

public class LoginController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;
    private SessionContext session;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisibleField;

    @FXML
    private ToggleButton passwordVisibilityToggle;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        // Initialize password visibility toggle
        if (passwordVisibilityToggle != null) {
            // Add listener to toggle button
            passwordVisibilityToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    // Show password
                    passwordVisibleField.setText(passwordField.getText());
                    passwordField.setVisible(false);
                    passwordField.setManaged(false);
                    passwordVisibleField.setVisible(true);
                    passwordVisibleField.setManaged(true);
                    passwordVisibilityToggle.setGraphic(createLabel("🔒"));
                } else {
                    // Hide password
                    passwordField.setText(passwordVisibleField.getText());
                    passwordVisibleField.setVisible(false);
                    passwordVisibleField.setManaged(false);
                    passwordField.setVisible(true);
                    passwordField.setManaged(true);
                    passwordVisibilityToggle.setGraphic(createLabel("👁"));
                }
            });
        }
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 16px;");
        return label;
    }

    private String getPasswordValue() {
        if (passwordVisibilityToggle != null && passwordVisibilityToggle.isSelected()) {
            return passwordVisibleField.getText();
        }
        return passwordField.getText();
    }

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
        try {
            navigator.show(UiView.PATIENT_REGISTRATION);
        } catch (RuntimeException ex) {
            statusLabel.setText("Unable to open registration");
            UiUtils.showError("Registration failed", ex.getMessage());
        }
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
        if (passwordVisibleField != null) {
            passwordVisibleField.clear();
        }
        if (passwordVisibilityToggle != null) {
            passwordVisibilityToggle.setSelected(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            if (passwordVisibleField != null) {
                passwordVisibleField.setVisible(false);
                passwordVisibleField.setManaged(false);
            }
        }
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
        String password = getPasswordValue() == null ? "" : getPasswordValue();
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
