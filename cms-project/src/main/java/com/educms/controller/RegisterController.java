package com.educms.controller;

import com.educms.model.Role;
import com.educms.service.AuthService;
import com.educms.util.AlertUtil;
import com.educms.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        // AUTHOR is the sensible default self-serve role; ADMIN accounts are
        // provisioned by an existing admin via User Management instead.
        roleCombo.getItems().addAll(Role.AUTHOR, Role.EDITOR);
        roleCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleRegister() {
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match.");
            return;
        }
        try {
            authService.register(
                    usernameField.getText(),
                    passwordField.getText(),
                    fullNameField.getText(),
                    emailField.getText(),
                    roleCombo.getValue()
            );
            AlertUtil.info("Account Created", "Your account was created successfully. You can now sign in.");
            goToLogin();
        } catch (AuthService.AuthException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        SceneManager.switchScene("/com/educms/view/login.fxml", "Educational CMS - Sign In");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }
}
