package com.example.gui.controllers;

import com.example.client.StartSMSClient;
import com.example.gui.enums.StagePath;
import com.example.gui.enums.ThemesPath;
import com.example.gui.utils.Loader;
import com.example.server.models.Employee;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.network.ServerClient;
import com.example.server.utils.GsonHolder;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;

import static com.example.gui.utils.AlertUtil.error;

public class LoginController {
    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button regButton;

    @FXML
    private ComboBox<ThemesPath> themesComboBox;

    @FXML
    private TextField usernameField;

    private ServerClient serverClient;


    @FXML
    public void initialize() {

        themesComboBox.setItems(FXCollections.observableArrayList(ThemesPath.values()));
        themesComboBox.setPromptText(StartSMSClient.themeName);
        // Получаем существующий экземпляр ServerClient
        serverClient = ServerClient.getInstance();
        // Проверяем, установлено ли соединение

        if (!serverClient.isConnected()) {
            error("Ошибка", "Нет соединения с сервером");
        }
    }

    @FXML
    void onLoginButton(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            error("Ошибка", "Логин и пароль обязательны");
            return;
        }

        // Создаем JSONObject с данными
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("password", password);

        // Отправляем запрос на сервер
        Request request = new Request(com.example.server.enums.Operation.LOGIN, data.toString());
        Response response = serverClient.sendRequest(request);

        if (response.isSuccess()) {
            String department = GsonHolder.getGson().fromJson(response.getData(), Employee.class).getDepartment().getName().trim();

            if (department.equals("Бухгалтерия")) {
                System.out.println("Вход от имени бухгалтера.");
                Loader.loadScene((Stage) regButton.getScene().getWindow(), StagePath.MENU_ADMIN);

            } else {
                System.out.println("Вход от имени пользователя.");
                UserMenuController.getInstance().setCurrentEmployee(GsonHolder.getGson().fromJson(response.getData(), Employee.class));
                Loader.loadScene((Stage) regButton.getScene().getWindow(), StagePath.MENU_USER);

            }
        } else {
            error("Ошибка", "Не удалось войти: " + response.getMessage());
        }

//        Loader.loadScene((Stage) regButton.getScene().getWindow(), StagePath.MENU_ADMIN);

    }

    @FXML
    void onThemesComboBox(ActionEvent event) {
        ThemesPath selectedTheme = themesComboBox.getValue();
        if (selectedTheme != null) {
            StartSMSClient.themeName = selectedTheme.name();
            Loader.reloadForTheme();
        }
    }

    @FXML
    void onRegButton(ActionEvent event) {
        Loader.loadScene((Stage) regButton.getScene().getWindow(), StagePath.REGISTRATION);
    }
}