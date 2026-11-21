package com.educms.controller;

import com.educms.dao.ActivityLogDAO;
import com.educms.dao.ContentDAO;
import com.educms.dao.MediaDAO;
import com.educms.dao.UserDAO;
import com.educms.model.ContentStatus;
import com.educms.model.ContentType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML private Label articleCountLabel;
    @FXML private Label blogCountLabel;
    @FXML private Label mediaCountLabel;
    @FXML private Label userCountLabel;
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> typeBarChart;
    @FXML private ListView<String> activityListView;

    private final ContentDAO contentDAO = new ContentDAO();
    private final MediaDAO mediaDAO = new MediaDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, HH:mm");

    @FXML
    private void initialize() {
        int articleCount = contentDAO.countByType(ContentType.ARTICLE);
        int blogCount = contentDAO.countByType(ContentType.BLOG);

        articleCountLabel.setText(String.valueOf(articleCount));
        blogCountLabel.setText(String.valueOf(blogCount));
        mediaCountLabel.setText(String.valueOf(mediaDAO.countAll()));
        userCountLabel.setText(String.valueOf(userDAO.countAll()));

        statusPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Draft", contentDAO.countByStatus(ContentStatus.DRAFT)),
                new PieChart.Data("Published", contentDAO.countByStatus(ContentStatus.PUBLISHED)),
                new PieChart.Data("Archived", contentDAO.countByStatus(ContentStatus.ARCHIVED))
        ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Articles", articleCount));
        series.getData().add(new XYChart.Data<>("Blogs", blogCount));
        typeBarChart.getData().add(series);

        var entries = activityLogDAO.recent(15);
        var display = entries.stream()
                .map(e -> String.format("%s  •  %s  —  %s%s",
                        e.timestamp() != null ? e.timestamp().format(FMT) : "-",
                        e.userName() != null ? e.userName() : "Unknown",
                        e.action(),
                        e.details() != null && !e.details().isBlank() ? " (" + e.details() + ")" : ""))
                .toList();
        activityListView.setItems(FXCollections.observableArrayList(display));
    }
}
