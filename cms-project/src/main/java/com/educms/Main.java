package com.educms;

import com.educms.dao.DatabaseManager;
import com.educms.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for the Educational CMS JavaFX application.
 * Boots the embedded database then hands off to the login view.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.initializeDatabase();

        SceneManager.init(primaryStage);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        SceneManager.switchScene("/com/educms/view/login.fxml", "Educational CMS - Sign In");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
