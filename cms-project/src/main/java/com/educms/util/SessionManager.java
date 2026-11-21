package com.educms.util;

import com.educms.model.Role;
import com.educms.model.User;

/** Holds the currently authenticated user for the lifetime of the app session. */
public class SessionManager {

    private static User currentUser;

    private SessionManager() {}

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasRole(Role... roles) {
        if (currentUser == null) return false;
        for (Role r : roles) {
            if (currentUser.getRole() == r) return true;
        }
        return false;
    }

    /** ADMIN and EDITOR can manage any content; AUTHOR only their own. */
    public static boolean canEditAnyContent() {
        return hasRole(Role.ADMIN, Role.EDITOR);
    }

    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }
}
