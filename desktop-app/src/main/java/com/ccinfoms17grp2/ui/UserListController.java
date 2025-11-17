package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.services.UserService;
import com.ccinfoms17grp2.utils.DateTimeUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserListController implements Initializable {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> userIdColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> userTypeColumn;
    @FXML
    private TableColumn<User, Integer> personIdColumn;
    @FXML
    private TableColumn<User, Boolean> isActiveColumn;
    @FXML
    private TableColumn<User, LocalDateTime> lastLoginColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button createButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button backButton;

    private final ServiceRegistry services;
    private final User currentUser;
    private final ExecutorService executorService;
    private final ObservableList<User> users;

    public UserListController(ServiceRegistry services, User currentUser) {
        this.services = services;
        this.currentUser = currentUser;
        this.executorService = Executors.newCachedThreadPool();
        this.users = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        userTable.setItems(users);
        
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
        
        loadData();
    }

    private void setupTableColumns() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userTypeColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue().getUserType() != null ? cellData.getValue().getUserType().name() : ""
            )
        );
        personIdColumn.setCellValueFactory(new PropertyValueFactory<>("personId"));
        isActiveColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        isActiveColumn.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "✓ Active" : "✗ Inactive");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
        lastLoginColumn.setCellValueFactory(new PropertyValueFactory<>("lastLoginAt"));
        lastLoginColumn.setCellFactory(column -> new TableCell<User, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DateTimeUtil.formatDateTime(item));
                }
            }
        });
    }

    private void loadData() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() {
                UserService userService = services.getUserService();
                return userService.listUsers();
            }
        };
        
        task.setOnSucceeded(event -> {
            users.clear();
            users.addAll(task.getValue());
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to load users", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            loadData();
            return;
        }
        
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() {
                UserService userService = services.getUserService();
                return userService.searchUsers(keyword.trim());
            }
        };
        
        task.setOnSucceeded(event -> {
            users.clear();
            users.addAll(task.getValue());
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to search users", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleCreate() {
        openUserForm(null);
    }

    @FXML
    private void handleEdit() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openUserForm(selected);
        }
    }

    @FXML
    private void handleDelete() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        
        boolean confirmed = UiUtils.showConfirmation(
            "Delete User",
            "Are you sure you want to delete user: " + selected.getEmail() + "?"
        );
        
        if (!confirmed) {
            return;
        }
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                UserService userService = services.getUserService();
                userService.deleteUser(selected.getUserId());
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            UiUtils.showInfo("Success", "User deleted successfully");
            loadData();
        });
        
        task.setOnFailed(event -> {
            UiUtils.showError("Error", "Failed to delete user", task.getException());
        });
        
        executorService.execute(task);
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleBack() {
        navigateToAdminDashboard();
    }

    @SuppressWarnings("UseSpecificCatch")
    private void openUserForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/user-form.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == UserFormController.class) {
                    return new UserFormController(services, currentUser, user);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/ccinfoms17grp2/ui/app.css").toExternalForm());
            
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(user == null ? "Create User" : "Edit User");
            stage.setResizable(false);
            stage.showAndWait();
            
            // Refresh list after form closes
            loadData();
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to open user form", ex);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    private void navigateToAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccinfoms17grp2/ui/admin-dashboard.fxml"));
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AdminDashboardController.class) {
                    return new AdminDashboardController(services, currentUser);
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to instantiate controller: " + controllerClass, ex);
                }
            });
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/ccinfoms17grp2/ui/app.css").toExternalForm());
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Portal");
            
        } catch (IOException ex) {
            UiUtils.showError("Navigation Error", "Failed to navigate to admin dashboard", ex);
        }
    }
}
