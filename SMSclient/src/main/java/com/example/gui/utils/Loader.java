package com.example.gui.utils;

import com.example.client.StartSMSClient;
import com.example.gui.enums.StagePath;
import com.example.gui.enums.ThemesPath;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Loader {
    public static void loadSceneWithThrowException(Stage stage, StagePath stagePath) throws IOException {
        FXMLLoader loader = new FXMLLoader(Loader.class.getResource(stagePath.getPathToFxml()));
        Parent root = loader.load();

        stage.setResizable(true);
        Scene scene = new Scene(root);

        // Проверка и применение темы
        String theme = StartSMSClient.themeName.toUpperCase();
        try {
            ThemesPath themePath = ThemesPath.valueOf(theme);
            String cssPath = themePath.getPathToCss();
            // Удаляем '@' из пути, если он есть, и проверяем корректность пути
            if (cssPath.startsWith("@")) {
                cssPath = cssPath.substring(1); // Убираем '@'
            }
            scene.getStylesheets().add(Objects.requireNonNull(Loader.class.getResource(cssPath)).toExternalForm());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid theme: " + StartSMSClient.themeName + ". Falling back to default theme (LIGHT).");
            scene.getStylesheets().add(Objects.requireNonNull(Loader.class.getResource(ThemesPath.LIGHT.getPathToCss())).toExternalForm());
        }

        stage.setScene(scene);
    }

    public static void loadScene(Stage stage, StagePath stagePath) {
        try {
            loadSceneWithThrowException(stage, stagePath);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.error("Navigation Error", "Could not navigate.");
        }
    }

    public static void reloadForTheme() {
        loadScene(StartSMSClient.getPrimaryStage(), StagePath.LOGIN);
    }
}