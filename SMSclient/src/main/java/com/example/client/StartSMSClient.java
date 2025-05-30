package com.example.client;

import com.example.gui.enums.StagePath;
import com.example.gui.utils.Loader;
import com.example.server.enums.Operation;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.network.ServerClient;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;
import java.util.Objects;

public class StartSMSClient extends Application {
    public static String themeName = "BLUE";
    @Getter
    private static Stage primaryStage;
    private ServerClient client;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        client = ServerClient.getInstance();
        try {
            client.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png"))));
            primaryStage.setTitle("Система управления заработной платой");
            if (client.isConnected()) {
                Loader.loadSceneWithThrowException(primaryStage, StagePath.LOGIN);
            } else {
                Loader.loadSceneWithThrowException(primaryStage, StagePath.CONNECTION_CHECK);
            }
        } catch (IOException e) {
            System.err.println("Ошибка запуска клиента: " + e.getMessage());
            e.printStackTrace();
        }

        primaryStage.show();
    }

    @Override
    public void stop() {
        if (client != null) {
            Request request = new Request(Operation.DISCONNECT, null);
            Response response = client.sendRequest(request);
            client.disconnect();
        }
    }
}