package com.example.gui.controllers;

import com.example.client.utils.ServerConfig;
import com.example.gui.enums.StagePath;
import com.example.gui.utils.AlertUtil;
import com.example.gui.utils.Loader;
import com.example.server.network.ServerClient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


public class CheckController {
    private final Image successImage = new Image(getClass().getResourceAsStream("/images/galka.png"));
    private final Image failureImage = new Image(getClass().getResourceAsStream("/images/krest.png"));
    @FXML
    private TextField ipField;
    @FXML
    private TextField portField;
    @FXML
    private Label statusLabel;
    @FXML
    private ImageView statusImageView;
    @FXML
    private Button nextButton;

    @FXML
    public void initialize() {
        ipField.setText(ServerConfig.getIp());
        portField.setText(String.valueOf(ServerConfig.getPort()));
        updateConnectionStatus();
    }

    private void updateConnectionStatus() {
        String ip = ipField.getText();
        try {
            int port = Integer.parseInt(portField.getText());
            boolean connected = ServerClient.getInstance().checkConnection(ip, port);
            if (connected) {
                statusLabel.setText("установлено");
                statusLabel.setStyle("-fx-text-fill: green;");
                statusImageView.setImage(successImage);
                nextButton.setDisable(false);
            } else {
                statusLabel.setText("не установлено");
                statusLabel.setStyle("-fx-text-fill: red;");
                statusImageView.setImage(failureImage);
                nextButton.setDisable(true);
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("не установлено");
            statusLabel.setStyle("-fx-text-fill: red;");
            statusImageView.setImage(failureImage);
            nextButton.setDisable(true);
            AlertUtil.error("Ошибка", "Порт должен быть числом");
        }
//         catch (IOException e) {
//            statusLabel.setText("не установлено");
//            statusLabel.setStyle("-fx-text-fill: red;");
//            statusImageView.setImage(failureImage);
//            nextButton.setDisable(true);
//        }
    }

    @FXML
    private void onReconnect() {
        String ip = ipField.getText();
        String portStr = portField.getText();
        try {
            int port = Integer.parseInt(portStr);
            ServerConfig.setIp(ip);
            ServerConfig.setPort(port);
            ServerConfig.saveConfig();
            updateConnectionStatus();
        } catch (NumberFormatException e) {
            AlertUtil.error("Ошибка", "Порт должен быть числом");
        }
    }

    @FXML
    private void onNext() {
        Loader.loadScene((Stage) nextButton.getScene().getWindow(), StagePath.LOGIN);
    }
}