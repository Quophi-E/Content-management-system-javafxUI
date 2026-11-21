package com.educms.controller;

import com.educms.dao.ActivityLogDAO;
import com.educms.dao.MediaDAO;
import com.educms.model.MediaFile;
import com.educms.util.AlertUtil;
import com.educms.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class MediaListController {

    @FXML private TextField searchField;
    @FXML private TableView<MediaFile> tableView;
    @FXML private TableColumn<MediaFile, String> filenameColumn;
    @FXML private TableColumn<MediaFile, String> typeColumn;
    @FXML private TableColumn<MediaFile, String> sizeColumn;
    @FXML private TableColumn<MediaFile, String> uploaderColumn;
    @FXML private TableColumn<MediaFile, String> uploadedColumn;
    @FXML private TableColumn<MediaFile, Void> actionsColumn;

    private final MediaDAO mediaDAO = new MediaDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    @FXML
    private void initialize() {
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("fileType"));
        sizeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReadableSize()));
        uploaderColumn.setCellValueFactory(new PropertyValueFactory<>("uploaderName"));
        uploadedColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUploadedAt() != null ? data.getValue().getUploadedAt().format(FMT) : "—"));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button openBtn = new Button("Open");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, openBtn, deleteBtn);
            {
                openBtn.setOnAction(e -> openFile(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                openBtn.getStyleClass().add("table-action-button");
                deleteBtn.getStyleClass().addAll("table-action-button", "danger-button");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
        refresh();
    }

    private void refresh() {
        tableView.setItems(FXCollections.observableArrayList(mediaDAO.findAll()));
    }

    private void applyFilter() {
        String kw = searchField.getText();
        tableView.setItems(FXCollections.observableArrayList(
                (kw == null || kw.isBlank()) ? mediaDAO.findAll() : mediaDAO.search(kw.trim())));
    }

    @FXML
    private void handleUpload() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/educms/view/media_upload.fxml"));
            Parent root = loader.load();
            MediaUploadController controller = loader.getController();
            controller.setOnUploaded(this::refresh);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Upload Media File");
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            AlertUtil.error("Error", "Could not open upload dialog: " + e.getMessage());
        }
    }

    private void openFile(MediaFile media) {
        try {
            Desktop.getDesktop().open(new File(media.getFilepath()));
        } catch (Exception e) {
            AlertUtil.error("Could not open file", "Unable to open " + media.getFilename() + ": " + e.getMessage());
        }
    }

    private void handleDelete(MediaFile media) {
        if (AlertUtil.confirm("Delete File", "Delete \"" + media.getFilename() + "\" from the library?")) {
            mediaDAO.delete(media.getId());
            activityLogDAO.log(SessionManager.getCurrentUser().getId(), "DELETE_MEDIA", media.getFilename());
            refresh();
        }
    }
}
