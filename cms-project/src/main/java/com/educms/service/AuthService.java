package com.educms.service;

import com.educms.dao.ActivityLogDAO;
import com.educms.dao.UserDAO;
import com.educms.model.Role;
import com.educms.model.User;
import com.educms.util.PasswordUtil;
import com.educms.util.SessionManager;

import java.util.Optional;

/** Business logic for authentication and account creation. Controllers call
 *  into this rather than talking to DAOs directly, keeping the M-V-C split clean. */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public static class AuthException extends Exception {
        public AuthException(String message) { super(message); }
    }

    public User login(String username, String password) throws AuthException {
        Optional<User> found = userDAO.findByUsername(username);
        if (found.isEmpty() || !PasswordUtil.verify(password, found.get().getPasswordHash())) {
            throw new AuthException("Invalid username or password.");
        }
        User user = found.get();
        SessionManager.login(user);
        userDAO.touchLastLogin(user.getId());
        activityLogDAO.log(user.getId(), "LOGIN", "User signed in");
        return user;
    }

    public void logout() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            activityLogDAO.log(user.getId(), "LOGOUT", "User signed out");
        }
        SessionManager.logout();
    }

    public void register(String username, String password, String fullName, String email, Role role)
            throws AuthException {
        if (username == null || username.isBlank()) throw new AuthException("Username is required.");
        if (fullName == null || fullName.isBlank()) throw new AuthException("Full name is required.");
        if (!PasswordUtil.isStrongEnough(password)) {
            throw new AuthException("Password must be at least 8 characters and include a letter and a digit.");
        }
        if (userDAO.findByUsername(username).isPresent()) {
            throw new AuthException("That username is already taken.");
        }
        User u = new User();
        u.setUsername(username.trim());
        u.setPasswordHash(PasswordUtil.hash(password));
        u.setFullName(fullName.trim());
        u.setEmail(email);
        u.setRole(role);
        userDAO.insert(u);
    }
}
