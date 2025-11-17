package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.User;

public class SessionContext {

    private User currentUser;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isUserType(User.UserType userType) {
        return isLoggedIn() && currentUser.getUserType() == userType;
    }

    public int getPersonIdOrThrow(User.UserType expectedType) {
        if (!isUserType(expectedType)) {
            throw new IllegalStateException("Session does not contain " + expectedType + " user");
        }
        return currentUser.getPersonId();
    }

    public void clear() {
        currentUser = null;
    }
}
