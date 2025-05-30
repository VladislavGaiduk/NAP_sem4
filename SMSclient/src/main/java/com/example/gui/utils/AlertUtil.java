package com.example.gui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class AlertUtil {
    private String header;
    private String content;
    private Alert.AlertType alertType;

    public static void error(String header, String content) {
        AlertUtil.builder()
                .alertType(Alert.AlertType.ERROR)
                .header(header)
                .content(content)
                .build().realise();
    }

    public static void warning(String header, String content) {
        AlertUtil.builder()
                .alertType(Alert.AlertType.WARNING)
                .header(header)
                .content(content)
                .build().realise();
    }

    public static void info(String header, String content) {
        AlertUtil.builder()
                .alertType(Alert.AlertType.INFORMATION)
                .header(header)
                .content(content)
                .build().realise();
    }

    public static ButtonType confirmation(String header, String content) {
        return AlertUtil.builder()
                .alertType(Alert.AlertType.CONFIRMATION)
                .header(header)
                .content(content)
                .build().realiseWithConfirmation();
    }

    public void realise() {
        complete().showAndWait();
    }

    public ButtonType realiseWithConfirmation() {
        return complete().showAndWait().orElse(ButtonType.CANCEL);
    }

    public Alert complete() {
        Alert alert = new Alert(alertType);
        alert.setTitle("Система управления заработной платой");
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Устанавливаем иконку для Stage диалогового окна
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            stage.getIcons().clear(); // Очищаем существующие иконки
            stage.getIcons().add(icon); // Устанавливаем новую иконку
        } catch (Exception e) {
            System.err.println("Ошибка при установке иконки: " + e.getMessage());
        }
        return alert;
    }
}

