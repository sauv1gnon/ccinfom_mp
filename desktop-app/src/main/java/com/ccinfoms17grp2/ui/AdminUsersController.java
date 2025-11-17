package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.ValidationException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AdminUsersController implements ViewController {
    private SceneNavigator navigator;
    private ServiceRegistry services;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Integer> userIdColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, User.UserType> userTypeColumn;

    @FXML
    private TableColumn<User, Boolean> activeColumn;

    @FXML
    private TableColumn<User, LocalDateTime> createdAtColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userTypeColumn.setCellValueFactory(new PropertyValueFactory<>("userType"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        activeColumn.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                }
            }
        });

        createdAtColumn.setCellFactory(column -> new TableCell<User, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    @FXML
    private void handleAddUser() {
        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add User");
        dialog.setHeaderText("Create a new user account");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        ChoiceBox<User.UserType> userTypeChoice = new ChoiceBox<>(FXCollections.observableArrayList(User.UserType.values()));
        userTypeChoice.setValue(User.UserType.PATIENT);
        TextField personIdField = new TextField();
        personIdField.setPromptText("Person ID (Patient/Doctor ID)");
        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("User Type:"), 0, 2);
        grid.add(userTypeChoice, 1, 2);
        grid.add(new Label("Person ID:"), 0, 3);
        grid.add(personIdField, 1, 3);
        grid.add(activeCheck, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                User user = new User();
                user.setEmail(emailField.getText() == null ? "" : emailField.getText().trim());
                user.setUserType(userTypeChoice.getValue());
                user.setActive(activeCheck.isSelected());
                try {
                    String personIdText = personIdField.getText() == null ? "" : personIdField.getText().trim();
                    if (!personIdText.isEmpty()) {
                        user.setPersonId(Integer.parseInt(personIdText));
                    }
                } catch (NumberFormatException e) {
                    user.setPersonId(0);
                }
                return user;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            String password = passwordField.getText();
            if (user.getEmail().isEmpty() || password.isEmpty()) {
                statusLabel.setText("Email and password are required");
                return;
            }
            try {
                user.setPasswordHash(password);
                User created = services.getUserService().createUser(user);
                if (!user.isActive()) {
                    services.getUserService().updateActiveStatus(created.getUserId(), false);
                }
                statusLabel.setText("User created successfully: " + created.getEmail());
                loadUsers();
            } catch (ValidationException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to create user");
                UiUtils.showError("Create User Failed", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleEditUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a user to edit");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user: " + selected.getEmail());

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        TextField emailField = new TextField(selected.getEmail());
        emailField.setPromptText("Email");
        ChoiceBox<User.UserType> userTypeChoice = new ChoiceBox<>(FXCollections.observableArrayList(User.UserType.values()));
        userTypeChoice.setValue(selected.getUserType());
        TextField personIdField = new TextField(String.valueOf(selected.getPersonId()));
        personIdField.setPromptText("Person ID");
        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(selected.isActive());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("User Type:"), 0, 1);
        grid.add(userTypeChoice, 1, 1);
        grid.add(new Label("Person ID:"), 0, 2);
        grid.add(personIdField, 1, 2);
        grid.add(activeCheck, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                User user = new User();
                user.setUserId(selected.getUserId());
                user.setEmail(emailField.getText() == null ? "" : emailField.getText().trim());
                user.setUserType(userTypeChoice.getValue());
                user.setActive(activeCheck.isSelected());
                try {
                    String personIdText = personIdField.getText() == null ? "" : personIdField.getText().trim();
                    if (!personIdText.isEmpty()) {
                        user.setPersonId(Integer.parseInt(personIdText));
                    }
                } catch (NumberFormatException e) {
                    user.setPersonId(selected.getPersonId());
                }
                return user;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            if (user.getEmail().isEmpty()) {
                statusLabel.setText("Email is required");
                return;
            }
            try {
                services.getUserService().updateUser(user);
                if (user.isActive() != selected.isActive()) {
                    services.getUserService().updateActiveStatus(user.getUserId(), user.isActive());
                }
                statusLabel.setText("User updated successfully");
                loadUsers();
            } catch (ValidationException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to update user");
                UiUtils.showError("Update User Failed", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a user to delete");
            return;
        }

        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        boolean confirm = UiUtils.showConfirmation(
            "Delete User",
            "Are you sure you want to delete user: " + selected.getEmail() + "?"
        );

        if (confirm) {
            try {
                services.getUserService().deleteUser(selected.getUserId());
                statusLabel.setText("User deleted successfully");
                loadUsers();
            } catch (RuntimeException ex) {
                statusLabel.setText("Failed to delete user");
                UiUtils.showError("Delete User Failed", ex.getMessage());
            }
        }
    }

    @FXML
    private void handleSearch() {
        if (services == null) {
            statusLabel.setText("Services unavailable");
            return;
        }

        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        if (query.isEmpty()) {
            loadUsers();
            return;
        }

        try {
            java.util.Optional<User> userOpt = services.getUserService().findUserByEmail(query);
            if (userOpt.isPresent()) {
                usersTable.setItems(FXCollections.observableArrayList(userOpt.get()));
                statusLabel.setText("Found 1 user");
            } else {
                usersTable.setItems(FXCollections.observableArrayList());
                statusLabel.setText("No user found with email: " + query);
            }
        } catch (RuntimeException ex) {
            statusLabel.setText("Search failed");
            UiUtils.showError("Search Failed", ex.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadUsers();
    }

    @FXML
    private void handleBackToDashboard() {
        navigator.show(UiView.ADMIN_DASHBOARD);
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
    public void onDisplay() {
        statusLabel.setText("");
        loadUsers();
    }

    private void loadUsers() {
        if (services == null || usersTable == null) {
            return;
        }

        try {
            List<User> users = services.getUserService().listUsers();
            usersTable.setItems(FXCollections.observableArrayList(users));
            statusLabel.setText("Loaded " + users.size() + " users");
        } catch (RuntimeException ex) {
            statusLabel.setText("Failed to load users");
            UiUtils.showError("Load Users Failed", ex.getMessage());
        }
    }
}
