package com.example.gui.controllers;

import com.example.gui.enums.StagePath;
import com.example.gui.utils.AlertUtil;
import com.example.gui.utils.Loader;
import com.example.server.enums.Operation;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.network.ServerClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;

public class RegisterController {
    @FXML
    private Button backToLoginButton;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField patronymicField;

    @FXML
    private Button registerButton;

    @FXML
    private TextField usernameField;

    private ServerClient serverClient;

    @FXML
    public void initialize() {
        // Получаем существующий экземпляр ServerClient
        serverClient = ServerClient.getInstance();
        // Проверяем, установлено ли соединение
        if (!serverClient.isConnected()) {
            AlertUtil.error("Ошибка", "Нет соединения с сервером");
        }
    }

    @FXML
    void onBackToLoginButton(ActionEvent event) {
        Loader.loadScene((Stage) backToLoginButton.getScene().getWindow(), StagePath.LOGIN);
    }

    @FXML
    void onRegisterButton(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String patronymic = patronymicField.getText().trim();

        // Проверка на пустые поля
        if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            AlertUtil.error("Ошибка", "Пожалуйста, заполните все обязательные поля");
            return;
        }

        // Проверка совпадения паролей
        if (!password.equals(confirmPassword)) {
            AlertUtil.error("Ошибка", "Пароли не совпадают");
            return;
        }

        // Формируем запрос на сервер
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("passwordHash", password);
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("patronymic", patronymic);
        Request request = new Request(Operation.REGISTER, data.toString());

        // Отправляем запрос через ServerClient
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            AlertUtil.info("Успех", "Пользователь успешно зарегистрирован");
            Loader.loadScene((Stage) registerButton.getScene().getWindow(), StagePath.LOGIN);
        } else {
            AlertUtil.error("Ошибка", response.getMessage());
        }
    }
}