package com.educms.controller;

import com.educms.dao.ActivityLogDAO;
import com.educms.dao.ContentDAO;
import com.educms.model.ContentItem;
import com.educms.model.ContentStatus;
import com.educms.model.ContentType;
import com.educms.util.AlertUtil;
import com.educms.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/** Handles listing, searching, and CRUD entry points for both Articles and Blog posts
 *  -- the two content types share this one view, driven by {@link #setContentType}. */
public class ContentListController {

    @FXML private Label pageTitleLabel;
    @FXML private Label pageSubtitleLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<ContentStatus> statusFilterCombo;
    @FXML private TableView<ContentItem> tableView;
    @FXML private TableColumn<ContentItem, String> titleColumn;
    @FXML private TableColumn<ContentItem, String> authorColumn;
    @FXML private TableColumn<ContentItem, String> categoryColumn;
    @FXML private TableColumn<ContentItem, String> statusColumn;
    @FXML private TableColumn<ContentItem, String> updatedColumn;
    @FXML private TableColumn<ContentItem, Void> actionsColumn;

    private final ContentDAO contentDAO = new ContentDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private ContentType contentType;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    public void setContentType(ContentType type) {
        this.contentType = type;
        boolean isArticle = type == ContentType.ARTICLE;
        pageTitleLabel.setText(isArticle ? "Articles" : "Blog Posts");
        pageSubtitleLabel.setText(isArticle
                ? "Manage long-form articles and educational write-ups"
                : "Manage informal blog posts and updates");
        refresh();
    }

    @FXML
    private void initialize() {
        statusFilterCombo.setItems(FXCollections.observableArrayList(ContentStatus.values()));

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authorName"));
        categoryColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getCategory() == null ? "—" : data.getValue().getCategory()));
        statusColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().name()));
        updatedColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getUpdatedAt() != null ? data.getValue().getUpdatedAt().format(FMT) : "—"));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, deleteBtn);
            {
                editBtn.setOnAction(e -> openEditor(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                editBtn.getStyleClass().add("table-action-button");
                deleteBtn.getStyleClass().addAll("table-action-button", "danger-button");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                ContentItem row = getTableView().getItems().get(getIndex());
                boolean canEdit = SessionManager.canEditAnyContent()
                        || row.getAuthorId() == SessionManager.getCurrentUser().getId();
                editBtn.setDisable(!canEdit);
                deleteBtn.setDisable(!canEdit);
                setGraphic(box);
            }
        });

        searchField.textProperty().addListener((obs, old, val) -> applyFilters());
        statusFilterCombo.valueProperty().addListener((obs, old, val) -> applyFilters());
    }

    private void refresh() {
        tableView.setItems(FXCollections.observableArrayList(contentDAO.findByType(contentType)));
    }

    private void applyFilters() {
        var keyword = searchField.getText();
        var items = (keyword == null || keyword.isBlank())
                ? contentDAO.findByType(contentType)
                : contentDAO.search(contentType, keyword.trim());

        ContentStatus statusFilter = statusFilterCombo.getValue();
        if (statusFilter != null) {
            items = items.stream().filter(c -> c.getStatus() == statusFilter).collect(Collectors.toList());
        }
        tableView.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    private void handleNew() {
        openEditor(null);
    }

    private void openEditor(ContentItem existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/educms/view/content_edit.fxml"));
            Parent root = loader.load();
            ContentEditController controller = loader.getController();
            controller.setup(contentType, existing, this::refresh);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(existing == null ? "New " + contentType.name() : "Edit " + contentType.name());
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            AlertUtil.error("Error", "Could not open editor: " + e.getMessage());
        }
    }

    private void handleDelete(ContentItem item) {
        if (AlertUtil.confirm("Delete " + contentType.name(), "Delete \"" + item.getTitle() + "\"? This cannot be undone.")) {
            contentDAO.delete(item.getId());
            activityLogDAO.log(SessionManager.getCurrentUser().getId(), "DELETE_" + contentType.name(), item.getTitle());
            refresh();
        }
    }
}
