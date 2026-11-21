package com.educms.controller;

import com.educms.service.AuthService;
import com.educms.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showError("Please enter both username and password.");
            return;
        }

        try {
            authService.login(username.trim(), password);
            SceneManager.switchScene("/com/educms/view/main_layout.fxml", "Educational CMS - Dashboard");
        } catch (AuthService.AuthException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void goToRegister() {
        SceneManager.switchScene("/com/educms/view/register.fxml", "Educational CMS - Register");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }
}
