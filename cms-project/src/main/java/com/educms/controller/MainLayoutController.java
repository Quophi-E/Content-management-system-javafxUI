package com.educms.controller;

import com.educms.model.ContentType;
import com.educms.model.Role;
import com.educms.service.AuthService;
import com.educms.service.ReportService;
import com.educms.util.AlertUtil;
import com.educms.util.SceneManager;
import com.educms.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/** Controller for the persistent app shell: sidebar navigation + swappable content area. */
public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private Button navUsers;
    @FXML private Button navReports;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        var user = SessionManager.getCurrentUser();
        userLabel.setText(user.getFullName());
        roleLabel.setText(user.getRole().name());

        // Only admins manage users / pull system-wide reports.
        boolean isAdmin = SessionManager.isAdmin();
        navUsers.setVisible(isAdmin);
        navUsers.setManaged(isAdmin);
        navReports.setVisible(SessionManager.canEditAnyContent());
        navReports.setManaged(SessionManager.canEditAnyContent());

        showDashboard();
    }

    @FXML private void showDashboard() { load("/com/educms/view/dashboard.fxml"); }

    @FXML
    private void showArticles() {
        loadContentList(ContentType.ARTICLE);
    }

    @FXML
    private void showBlogs() {
        loadContentList(ContentType.BLOG);
    }

    private void loadContentList(ContentType type) {
        FXMLLoader loader = SceneManager.loadFragment("/com/educms/view/content_list.fxml");
        try {
            Parent view = loader.load();
            ContentListController controller = loader.getController();
            controller.setContentType(type);
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML private void showMedia() { load("/com/educms/view/media_list.fxml"); }

    @FXML private void showUsers() { load("/com/educms/view/user_management.fxml"); }

    @FXML
    private void showReports() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save CMS Report");
        chooser.setInitialFileName("cms_report.pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document", "*.pdf"));
        Stage stage = SceneManager.getPrimaryStage();
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;

        try {
            new ReportService().generateFullReport(file.getAbsolutePath());
            AlertUtil.info("Report Generated", "The PDF report was saved to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            AlertUtil.error("Report Failed", "Could not generate report: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        SceneManager.switchScene("/com/educms/view/login.fxml", "Educational CMS - Sign In");
    }

    private void load(String fxml) {
        try {
            Parent view = SceneManager.loadFragment(fxml).load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
