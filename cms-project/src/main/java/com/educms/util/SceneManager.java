package com.educms.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Centralizes FXML loading and scene switching so controllers don't need to
 * know about Stage plumbing directly. Follows the MVC "View" boundary: this
 * class only ever swaps root nodes / loads FXML, it holds no business logic.
 */
public class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {}

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    /** Loads a top-level view (login, register) and sets it as the whole scene. */
    public static void switchScene(String fxmlPath, String title) {
        try {
            URL url = SceneManager.class.getResource(fxmlPath);
            if (url == null) throw new IOException("FXML not found: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(SceneManager.class.getResource("/com/educms/css/style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fxmlPath, e);
        }
    }

    /** Loads an FXML fragment and returns its loader so a caller can grab the controller too. */
    public static FXMLLoader loadFragment(String fxmlPath) {
        try {
            URL url = SceneManager.class.getResource(fxmlPath);
            if (url == null) throw new IOException("FXML not found: " + fxmlPath);
            return new FXMLLoader(url);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load fragment: " + fxmlPath, e);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
