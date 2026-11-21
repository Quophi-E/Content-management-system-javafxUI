package com.educms.controller;

import com.educms.dao.ActivityLogDAO;
import com.educms.dao.UserDAO;
import com.educms.model.Role;
import com.educms.model.User;
import com.educms.util.AlertUtil;
import com.educms.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;

public class UserManagementController {

    @FXML private TableView<User> tableView;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> lastLoginColumn;
    @FXML private TableColumn<User, Void> actionsColumn;

    private final UserDAO userDAO = new UserDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().name()));
        lastLoginColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLastLogin() != null ? data.getValue().getLastLogin().format(FMT) : "Never"));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<Role> roleCombo = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
            private final Button deleteBtn = new Button("Remove");
            private final HBox box = new HBox(6, roleCombo, deleteBtn);
            {
                deleteBtn.getStyleClass().addAll("table-action-button", "danger-button");
                roleCombo.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (roleCombo.getValue() != user.getRole()) {
                        userDAO.updateRole(user.getId(), roleCombo.getValue());
                        activityLogDAO.log(SessionManager.getCurrentUser().getId(), "CHANGE_ROLE",
                                user.getUsername() + " -> " + roleCombo.getValue());
                    }
                });
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User user = getTableView().getItems().get(getIndex());
                roleCombo.getSelectionModel().select(user.getRole());
                boolean isSelf = user.getId() == SessionManager.getCurrentUser().getId();
                deleteBtn.setDisable(isSelf);
                roleCombo.setDisable(isSelf);
                setGraphic(box);
            }
        });

        refresh();
    }

    private void refresh() {
        tableView.setItems(FXCollections.observableArrayList(userDAO.findAll()));
    }

    private void handleDelete(User user) {
        if (AlertUtil.confirm("Remove User", "Remove account \"" + user.getFullName() + "\"? This cannot be undone.")) {
            userDAO.delete(user.getId());
            activityLogDAO.log(SessionManager.getCurrentUser().getId(), "DELETE_USER", user.getUsername());
            refresh();
        }
    }
}
