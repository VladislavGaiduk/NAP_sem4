package com.example.gui.controllers;

import com.example.client.StartSMSClient;
import com.example.gui.enums.StagePath;
import com.example.gui.enums.ThemesPath;
import com.example.gui.utils.Loader;
import com.example.server.enums.Operation;
import com.example.server.models.User;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.network.ServerClient;
import com.example.server.utils.GsonHolder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.example.gui.utils.AlertUtil.confirmation;
import static com.example.gui.utils.AlertUtil.error;

public class UsersController {
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Long> idColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> firstNameColumn;
    @FXML
    private TableColumn<User, String> lastNameColumn;
    @FXML
    private TableColumn<User, String> patronymicColumn;

    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField patronymicField;

    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button transferButton;

    private ServerClient serverClient;
    private ObservableList<User> usersList;

    @FXML
    private void initialize() {
        System.out.println("UsersController инициализирован в " + java.time.LocalDateTime.now());
        serverClient = ServerClient.getInstance();

        // Настройка столбцов таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        patronymicColumn.setCellValueFactory(new PropertyValueFactory<>("patronymic"));


        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        // Загрузка данных
        loadUsers();

        // Обработчик выбора строки
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                usernameField.setText(newSelection.getUsername());
                passwordField.setText(""); // Очищаем поле пароля
                firstNameField.setText(newSelection.getFirstName());
                lastNameField.setText(newSelection.getLastName());
                patronymicField.setText(newSelection.getPatronymic());
            } else {
                clearFields();
                // setButtonStates();
            }
        });

        // Начальное состояние кнопок
        //setButtonStates();
    }

    private void loadUsers() {
        Request request = new Request(Operation.GET_ALL_USERS, null);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            String jsonData = response.getData();
            if (jsonData != null && !jsonData.isEmpty()) {
                List<User> users = GsonHolder.getGson().fromJson(jsonData, new TypeToken<List<User>>() {
                }.getType());
                usersList = FXCollections.observableArrayList(users);
                usersTable.setItems(usersList);
            } else {
                usersList = FXCollections.observableArrayList();
                usersTable.setItems(usersList);
                error("Предупреждение", "Список пользователей пуст.");
            }
        } else {
            error("Ошибка", "Не удалось загрузить пользователей: " + response.getMessage());
        }
    }

    @FXML
    private void addUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String patronymic = patronymicField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            error("Ошибка", "Логин и пароль обязательны.");
            return;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(password);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPatronymic(patronymic);

        String data = GsonHolder.getGson().toJson(newUser);
        System.out.println("Отправляемый JSON для добавления: " + data);

        Request request = new Request(Operation.REGISTER, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadUsers();
            clearFields();
        } else {
            error("Ошибка", "Не удалось добавить пользователя: " + response.getMessage());
        }
    }

    @FXML
    private void updateUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            error("Ошибка", "Выберите пользователя для обновления.");
            return;
        }

        String newUsername = usernameField.getText().trim();
        String newPassword = passwordField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String patronymic = patronymicField.getText().trim();

        if (newUsername.isEmpty()) {
            error("Ошибка", "Логин обязателен.");
            return;
        }

        selectedUser.setUsername(newUsername);
        if (!newPassword.isEmpty()) {
            selectedUser.setPasswordHash(newPassword);
        }
        selectedUser.setFirstName(firstName);
        selectedUser.setLastName(lastName);
        selectedUser.setPatronymic(patronymic);

        String data = GsonHolder.getGson().toJson(selectedUser);
        System.out.println("Отправляемый JSON для обновления: " + data);

        Request request = new Request(Operation.UPDATE_USER, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadUsers();
            clearFields();
        } else {
            error("Ошибка", "Не удалось обновить пользователя: " + response.getMessage());
        }
    }

    @FXML
    private void transferUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            error("Ошибка", "Выберите пользователя для активации учётной записи.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(StagePath.ACTIVATION_USER.getPathToFxml()));
            Parent root = loader.load();
            ActivationWindowController controller = loader.getController(); // Ожидаем ActivationWindowController
            controller.setSelectedUser(selectedUser);


            Stage stage = new Stage();
            Scene scene = new Scene(root);
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
            stage.setTitle("Активация пользователя");
            controller.setStage(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void deleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            error("Ошибка", "Выберите заявку для удаления.");
            return;
        }

        ButtonType confirmation = confirmation("Удаление заявки", "Вы уверены, что хотите удалить заявку на пользователя " +
                selectedUser.getFirstName() + " " + selectedUser.getLastName() + "?");
        if (confirmation != ButtonType.OK) {
            return;
        }

        String data = GsonHolder.getGson().toJson(selectedUser.getId());
        System.out.println("Отправляемый JSON для удаления: " + data);

        Request request = new Request(Operation.DELETE_USER, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadUsers();
            clearFields();
        } else {
            error("Ошибка", "Не удалось удалить заявку: " + response.getMessage());
        }
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        patronymicField.clear();
    }

//    private void setButtonStates() {
//        boolean isSelected = usersTable.getSelectionModel().getSelectedItem() != null;
//        updateButton.setDisable(!isSelected);
//        deleteButton.setDisable(!isSelected);
//        transferButton.setDisable(!isSelected);
//    }
}