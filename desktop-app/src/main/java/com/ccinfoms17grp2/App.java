package com.ccinfoms17grp2;

import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.ui.SceneNavigator;
import com.ccinfoms17grp2.ui.SessionContext;
import com.ccinfoms17grp2.ui.UiView;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {

    private ServiceRegistry services;
    private SceneNavigator navigator;
    private SessionContext session;

    @Override
    public void start(Stage primaryStage) {
        services = new ServiceRegistry();
        session = new SessionContext();
        navigator = new SceneNavigator(primaryStage, services, session);
        navigator.show(UiView.LOGIN);

        // Adjust window size to fit screen
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(1100, screenBounds.getWidth() * 0.9);
        double height = Math.min(1019, screenBounds.getHeight() * 0.9);
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.centerOnScreen();
    }

    @Override
    public void stop() {
        navigator = null;
        services = null;
        session = null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
