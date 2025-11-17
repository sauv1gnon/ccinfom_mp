package com.ccinfoms17grp2;

import com.ccinfoms17grp2.services.ServiceRegistry;
import com.ccinfoms17grp2.ui.SceneNavigator;
import com.ccinfoms17grp2.ui.SessionContext;
import com.ccinfoms17grp2.ui.UiView;
import javafx.application.Application;
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
