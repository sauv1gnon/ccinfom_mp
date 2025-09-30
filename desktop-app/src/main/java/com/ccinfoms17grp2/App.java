package com.ccinfoms17grp2;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane(new Label("Digital Queue and Appointment System"));
        Scene scene = new Scene(root, 640, 360);
        primaryStage.setTitle("Digital Queue and Appointment System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
