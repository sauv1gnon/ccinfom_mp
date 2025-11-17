package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.AuthService;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.ValidationException;
import com.ccinfoms17grp2.utils.PasswordUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserFormController implements Initializable {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<User.UserType> userTypeComboBox;
    @FXML
    private TextField personIdField;
    @FXML
    private CheckBox isActiveCheckBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label titleLabel;

    private final ServiceRegistry services;
    private final User currentUser;
    private final User editingUser;
    private final ExecutorService executorService;

    public UserFormController(ServiceRegistry services, User currentUser, User editingUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.editingUser = editingUser;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userTypeComboBox.getItems().addAll(User.UserType.values());
        
        if (editingUser != null) {
            titleLabel.setText("Edit User");
            populateForm();
            passwordField.setPromptText("Leave blank to keep current password");
        } else {
            titleLabel.setText("Create User");
            isActiveCheckBox.setSelected(true);
        }
    }

    private void populateForm() {
        emailField.setText(editingUser.getEmail());
        userTypeComboBox.setValue(editingUser.getUserType());
        personIdField.setText(String.valueOf(editingUser.getPersonId()));
        isActiveCheckBox.setSelected(editingUser.isActive());
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                AuthService authService = services.getAuthService();
                
                if (editingUser == null) {
                    // Create new user
                    String email = emailField.getText().trim();
                    String password = passwordField.getText();
                    User.UserType userType = userTypeComboBox.getValue();
                    int personId = Integer.parseInt(personIdField.getText().trim());
                    boolean isActive = isActiveCheckBox.isSelected();
                    
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setPasswordHash(PasswordUtil.hashPassword(password));
                    newUser.setUserType(userType);
                    newUser.setPersonId(personId);
                    newUser.setActive(isActive);
                    
                    services.getUserService().createUser(newUser);
                } else {
                    editingUser.setEmail(emailField.getText().trim());
                    if (!passwordField.getText().isEmpty()) {
                        editingUser.setPasswordHash(PasswordUtil.hashPassword(passwordField.getText()));
                    }
                    editingUser.setUserType(userTypeComboBox.getValue());
                    editingUser.setPersonId(Integer.parseInt(personIdField.getText().trim()));
                    editingUser.setActive(isActiveCheckBox.isSelected());
                    
                    services.getUserService().updateUser(editingUser);
                }
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            UiUtils.showInfo("Success", editingUser == null ? "User created successfully" : "User updated successfully");
            closeForm();
        });
        
        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            if (ex instanceof ValidationException) {
                UiUtils.showError("Validation Error", ex.getMessage(), null);
            } else {
                UiUtils.showError("Error", "Failed to save user", ex);
            }
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private boolean validateForm() {
        if (emailField.getText().trim().isEmpty()) {
            UiUtils.showError("Validation Error", "Email is required", null);
            return false;
        }
        if (editingUser == null && passwordField.getText().isEmpty()) {
            UiUtils.showError("Validation Error", "Password is required for new users", null);
            return false;
        }
        if (userTypeComboBox.getValue() == null) {
            UiUtils.showError("Validation Error", "User type is required", null);
            return false;
        }
        String personIdText = personIdField.getText().trim();
        if (personIdText.isEmpty()) {
            UiUtils.showError("Validation Error", "Person ID is required", null);
            return false;
        }
        try {
            int personId = Integer.parseInt(personIdText);
            if (personId <= 0) {
                UiUtils.showError("Validation Error", "Person ID must be positive", null);
                return false;
            }
        } catch (NumberFormatException e) {
            UiUtils.showError("Validation Error", "Person ID must be a valid number", null);
            return false;
        }
        return true;
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
