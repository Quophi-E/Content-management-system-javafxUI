package com.educms.controller;

import com.educms.dao.ActivityLogDAO;
import com.educms.dao.ContentDAO;
import com.educms.model.ContentItem;
import com.educms.model.ContentStatus;
import com.educms.model.ContentType;
import com.educms.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ContentEditController {

    @FXML private Label dialogTitleLabel;
    @FXML private TextField titleField;
    @FXML private TextField categoryField;
    @FXML private TextField tagsField;
    @FXML private ComboBox<ContentStatus> statusCombo;
    @FXML private TextArea bodyArea;
    @FXML private Label errorLabel;

    private final ContentDAO contentDAO = new ContentDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    private ContentType contentType;
    private ContentItem editing; // null when creating new
    private Runnable onSaved;

    public void setup(ContentType type, ContentItem existing, Runnable onSaved) {
        this.contentType = type;
        this.editing = existing;
        this.onSaved = onSaved;

        statusCombo.setItems(FXCollections.observableArrayList(ContentStatus.values()));

        if (existing == null) {
            dialogTitleLabel.setText("New " + capitalize(type.name()));
            statusCombo.getSelectionModel().select(ContentStatus.DRAFT);
        } else {
            dialogTitleLabel.setText("Edit " + capitalize(type.name()));
            titleField.setText(existing.getTitle());
            categoryField.setText(existing.getCategory());
            tagsField.setText(existing.getTags());
            bodyArea.setText(existing.getBody());
            statusCombo.getSelectionModel().select(existing.getStatus());
        }
    }

    @FXML
    private void handleSave() {
        if (titleField.getText() == null || titleField.getText().isBlank()) {
            showError("Title is required.");
            return;
        }
        if (bodyArea.getText() == null || bodyArea.getText().isBlank()) {
            showError("Body content is required.");
            return;
        }

        boolean isNew = editing == null;
        ContentItem c = isNew ? new ContentItem() : editing;
        c.setType(contentType);
        c.setTitle(titleField.getText().trim());
        c.setCategory(emptyToNull(categoryField.getText()));
        c.setTags(emptyToNull(tagsField.getText()));
        c.setBody(bodyArea.getText());
        c.setStatus(statusCombo.getValue());

        if (isNew) {
            c.setAuthorId(SessionManager.getCurrentUser().getId());
            contentDAO.insert(c);
            activityLogDAO.log(SessionManager.getCurrentUser().getId(), "CREATE_" + contentType.name(), c.getTitle());
        } else {
            contentDAO.update(c);
            activityLogDAO.log(SessionManager.getCurrentUser().getId(), "UPDATE_" + contentType.name(), c.getTitle());
        }

        if (onSaved != null) onSaved.run();
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) titleField.getScene().getWindow()).close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private String capitalize(String s) {
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
