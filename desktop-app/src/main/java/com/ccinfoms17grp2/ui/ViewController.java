package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.services.ServiceRegistry;

public interface ViewController {
    void setNavigator(SceneNavigator navigator);

    void setServices(ServiceRegistry services);

    default void setSession(SessionContext session) {
    }

    default void onDisplay() {
    }
}
