package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.AuthService;
import com.ccinfoms17grp2.services.AuthenticationException;
import com.ccinfoms17grp2.services.ServiceRegistry;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    private final AuthService authService;
    private final ExecutorService executorService;
    private final StringProperty statusMessage = new SimpleStringProperty();
    private final ServiceRegistry services;

    public LoginController(ServiceRegistry services) {
        this.authService = services.getAuthService();
        this.executorService = Executors.newCachedThreadPool();
        this.services = services;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusLabel.textProperty().bind(statusMessage);
        
        loginButton.disableProperty().bind(
            emailField.textProperty().isEmpty()
                .or(passwordField.textProperty().isEmpty())
                .or(statusMessage.isEqualTo("Signing in..."))
        );

        passwordField.setOnAction(event -> handleLogin());
        
        Platform.runLater(() -> emailField.requestFocus());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showStatus("Please enter both email and password", true);
            return;
        }

        setLoadingState(true);
        showStatus("Signing in...", false);

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() {
                return authService.authenticate(email, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            User user = loginTask.getValue();
            showStatus("Login successful! Welcome " + user.getEmail(), false);
            
            Platform.runLater(() -> {
                emailField.clear();
                passwordField.clear();
                navigateToHomepage(user);
            });
            
            setLoadingState(false);
        });

        loginTask.setOnFailed(event -> {
            Throwable exception = loginTask.getException();
            String errorMessage = "Login failed";
            
            if (exception instanceof AuthenticationException) {
                AuthenticationException authException = (AuthenticationException) exception;
                errorMessage = authException.getMessage();
            }
            
            showStatus(errorMessage, true);
            UiUtils.showError("Login Failed", errorMessage, exception);
            setLoadingState(false);
        });

        executorService.execute(loginTask);
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleRegistration() {
        navigateToRegistration();
    }

    @SuppressWarnings("UseSpecificCatch")
    private void navigateToRegistration() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/registration.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == RegistrationController.class) {
                    return new RegistrationController(new com.ccinfoms17grp2.services.ServiceRegistry());
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to registration screen", ex);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    private void navigateToHomepage(User user) {
        Stage stage = (Stage) emailField.getScene().getWindow();
        
        if (user.getUserType() == User.UserType.ADMIN) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/admin-dashboard.fxml"));
                loader.setControllerFactory(controllerClass -> {
                    if (controllerClass == AdminDashboardController.class) {
                        return new AdminDashboardController(services, user);
                    }
                    try {
                        return controllerClass.getDeclaredConstructor().newInstance();
                    } catch (Exception ex) {
                        throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                    }
                });
                
                stage.getScene().setRoot(loader.load());
            } catch (Exception ex) {
                UiUtils.showError("Navigation Error", "Failed to navigate to admin dashboard", ex);
            }
        } else if (user.getUserType() == User.UserType.DOCTOR) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/doctor-dashboard.fxml"));
                loader.setControllerFactory(controllerClass -> {
                    if (controllerClass == DoctorDashboardController.class) {
                        return new DoctorDashboardController(services, user);
                    }
                    try {
                        return controllerClass.getDeclaredConstructor().newInstance();
                    } catch (Exception ex) {
                        throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                    }
                });
                
                stage.getScene().setRoot(loader.load());
            } catch (Exception ex) {
                UiUtils.showError("Navigation Error", "Failed to navigate to doctor dashboard", ex);
            }
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/homepage.fxml"));
                loader.setControllerFactory(controllerClass -> {
                    if (controllerClass == HomepageController.class) {
                        return new HomepageController(services, user);
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
        emailField.setDisable(isLoading);
        passwordField.setDisable(isLoading);
        
        if (isLoading) {
            loginButton.setText("Signing In...");
        } else {
            loginButton.setText("Sign In");
            statusLabel.setVisible(false);
        }
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}