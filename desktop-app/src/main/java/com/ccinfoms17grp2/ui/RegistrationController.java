package com.ccinfoms17grp2.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.ccinfoms17grp2.models.Patient;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.AuthService;
import com.ccinfoms17grp2.services.AuthenticationException;
import com.ccinfoms17grp2.services.PatientService;
import com.ccinfoms17grp2.services.ServiceRegistry;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class RegistrationController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField contactNumberField;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;

    private static final Pattern CONTACT_PATTERN = Pattern.compile("[+0-9\\- ]{7,15}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final PatientService patientService;
    private final AuthService authService;
    private final ExecutorService executorService;
    private final StringProperty statusMessage = new SimpleStringProperty();

    public RegistrationController(ServiceRegistry services) {
        this.patientService = services.getPatientService();
        this.authService = services.getAuthService();
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusLabel.textProperty().bind(statusMessage);
        
        registerButton.disableProperty().bind(
            emailField.textProperty().isEmpty()
                .or(passwordField.textProperty().isEmpty())
                .or(confirmPasswordField.textProperty().isEmpty())
                .or(firstNameField.textProperty().isEmpty())
                .or(lastNameField.textProperty().isEmpty())
                .or(contactNumberField.textProperty().isEmpty())
        );

        confirmPasswordField.setOnAction(event -> handleRegistration());
        Platform.runLater(() -> emailField.requestFocus());
    }

    @FXML
    private void handleRegistration() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String contactNumber = contactNumberField.getText().trim();

        if (!validateInput(email, password, confirmPassword, firstName, lastName, contactNumber)) {
            return;
        }

        setLoadingState(true);
        showStatus("Creating your account...", false);

        Task<User> registerTask = new Task<>() {
            @Override
            protected User call() {
                try {
                    Patient patient = new Patient();
                    patient.setFirstName(firstName);
                    patient.setLastName(lastName);
                    patient.setContactNumber(contactNumber);
                    patient.setEmail(email);

                    Patient createdPatient = patientService.createPatient(patient);
                    return authService.registerPatient(email, password, createdPatient.getPatientId());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        registerTask.setOnSucceeded(event -> {
            User user = registerTask.getValue();
            showStatus("Registration successful! Welcome " + user.getEmail(), false);
            
            Platform.runLater(() -> {
                clearForm();
                UiUtils.showInformation("Registration Successful", 
                    "Welcome " + user.getEmail() + "!\nYour patient account has been created successfully.\n\nYou can now sign in with your credentials.");
                navigateToLogin();
            });
            
            setLoadingState(false);
        });

        registerTask.setOnFailed(event -> {
            Throwable exception = registerTask.getException();
            String errorMessage = "Registration failed";
            
            if (exception instanceof RuntimeException && exception.getCause() instanceof AuthenticationException) {
                AuthenticationException authException = (AuthenticationException) exception.getCause();
                errorMessage = authException.getMessage();
            } else if (exception instanceof RuntimeException && exception.getCause() instanceof Exception) {
                Exception cause = (Exception) exception.getCause();
                if (cause.getMessage() != null) {
                    errorMessage = cause.getMessage();
                }
            }
            
            showStatus(errorMessage, true);
            UiUtils.showError("Registration Failed", errorMessage, exception);
            setLoadingState(false);
        });

        executorService.execute(registerTask);
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleBack() {
        navigateToLogin();
    }

    private boolean validateInput(String email, String password, String confirmPassword, 
                                  String firstName, String lastName, String contactNumber) {
        
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
            firstName.isEmpty() || lastName.isEmpty() || contactNumber.isEmpty()) {
            showStatus("All fields are required", true);
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showStatus("Please enter a valid email address", true);
            emailField.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            showStatus("Password must be at least 6 characters long", true);
            passwordField.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showStatus("Passwords do not match", true);
            confirmPasswordField.requestFocus();
            return false;
        }

        if (!CONTACT_PATTERN.matcher(contactNumber).matches()) {
            showStatus("Contact number must contain 7-15 digits and may include + or - characters", true);
            contactNumberField.requestFocus();
            return false;
        }

        return true;
    }

    private void showStatus(String message, boolean isError) {
        statusMessage.set(message);
        statusLabel.setVisible(true);
        
        if (isError) {
            statusLabel.getStyleClass().removeAll("status-success");
            if (!statusLabel.getStyleClass().contains("status-error")) {
                statusLabel.getStyleClass().add("status-error");
            }
        } else {
            statusLabel.getStyleClass().removeAll("status-error");
            if (!statusLabel.getStyleClass().contains("status-success")) {
                statusLabel.getStyleClass().add("status-success");
            }
        }
    }

    private void setLoadingState(boolean isLoading) {
        registerButton.setDisable(isLoading);
        backButton.setDisable(isLoading);
        emailField.setDisable(isLoading);
        passwordField.setDisable(isLoading);
        confirmPasswordField.setDisable(isLoading);
        firstNameField.setDisable(isLoading);
        lastNameField.setDisable(isLoading);
        contactNumberField.setDisable(isLoading);
        
        if (isLoading) {
            registerButton.setText("Creating Account...");
        } else {
            registerButton.setText("Create Account");
            statusLabel.setVisible(false);
        }
    }

    private void clearForm() {
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        contactNumberField.clear();
    }

    private void navigateToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/login.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == LoginController.class) {
                    return new LoginController(new com.ccinfoms17grp2.services.ServiceRegistry());
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to login screen", ex);
        }
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}