package com.ccinfoms17grp2.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public final class UiUtils {

    private UiUtils() {
    }

    public static void showError(String title, String message, Throwable throwable) {
        System.out.println(title + ": " + message);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.setContentText(message + (throwable != null ? "\n" + throwable.getMessage() : ""));
            alert.showAndWait();
        });
    }

    public static void showError(String title, String message) {
        showError(title, message, null);
    }

    public static void showError(String message) {
        showError("Error", message, null);
    }

    public static void showInformation(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showInfo(String title, String message) {
        showInformation(title, message);
    }

    public static void showInfo(String message) {
        showInformation("Information", message);
    }

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(ButtonType.OK, cancel);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void showWarning(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
