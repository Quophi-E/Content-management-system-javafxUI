package com.educms.controller;

import com.educms.dao.ActivityLogDAO;
import com.educms.dao.DatabaseManager;
import com.educms.dao.MediaDAO;
import com.educms.model.MediaFile;
import com.educms.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class MediaUploadController {

    @FXML private Label fileNameLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;

    private final MediaDAO mediaDAO = new MediaDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private File chosenFile;
    private Runnable onUploaded;

    public void setOnUploaded(Runnable callback) {
        this.onUploaded = callback;
    }

    @FXML
    private void handleChooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a file to upload");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt")
        );
        File file = chooser.showOpenDialog(fileNameLabel.getScene().getWindow());
        if (file != null) {
            chosenFile = file;
            fileNameLabel.setText(file.getName());
        }
    }

    @FXML
    private void handleUpload() {
        if (chosenFile == null) {
            showError("Please choose a file first.");
            return;
        }
        try {
            Path mediaStore = DatabaseManager.getMediaStoreDir();
            String storedName = System.currentTimeMillis() + "_" + chosenFile.getName();
            Path destination = mediaStore.resolve(storedName);
            Files.copy(chosenFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            MediaFile media = new MediaFile();
            media.setFilename(chosenFile.getName());
            media.setFilepath(destination.toAbsolutePath().toString());
            media.setFileType(extensionOf(chosenFile.getName()));
            media.setFileSize(Files.size(destination));
            media.setDescription(descriptionArea.getText());
            media.setUploadedBy(SessionManager.getCurrentUser().getId());

            mediaDAO.insert(media);
            activityLogDAO.log(SessionManager.getCurrentUser().getId(), "UPLOAD_MEDIA", media.getFilename());

            if (onUploaded != null) onUploaded.run();
            closeDialog();
        } catch (IOException e) {
            showError("Upload failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) fileNameLabel.getScene().getWindow()).close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toUpperCase() : "FILE";
    }
}
