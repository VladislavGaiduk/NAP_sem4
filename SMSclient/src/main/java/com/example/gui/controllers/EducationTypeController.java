package com.example.gui.controllers;

import com.example.server.enums.Operation;
import com.example.server.models.EducationType;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.network.ServerClient;
import com.example.server.utils.GsonHolder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.List;

import static com.example.gui.utils.AlertUtil.confirmation;
import static com.example.gui.utils.AlertUtil.error;

public class EducationTypeController {

    @FXML
    private TableView<EducationType> educationTypesTable;
    @FXML
    private TableColumn<EducationType, Integer> idColumn;
    @FXML
    private TableColumn<EducationType, String> nameColumn;
    @FXML
    private TableColumn<EducationType, BigDecimal> coefficientColumn;

    @FXML
    private TextField nameField;
    @FXML
    private TextField coefficientField;

    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private ServerClient serverClient;
    private ObservableList<EducationType> educationTypesList;

    @FXML
    private void initialize() {
        System.out.println("EducationTypeController инициализирован в " + java.time.LocalDateTime.now());
        serverClient = ServerClient.getInstance();

        // Настройка столбцов таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        coefficientColumn.setCellValueFactory(new PropertyValueFactory<>("coefficient"));


        educationTypesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Загрузка данных
        loadEducationTypes();

        // Обработчик выбора строки
        educationTypesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                coefficientField.setText(newSelection.getCoefficient().toString());
            } else {
                clearFields();
            }
            setButtonStates();
        });

        // Начальное состояние кнопок
        setButtonStates();
    }

    private void loadEducationTypes() {
        Request request = new Request(Operation.GET_ALL_EDUCATION_TYPES, null);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            String jsonData = response.getData();
            if (jsonData != null && !jsonData.isEmpty()) {
                List<EducationType> educationTypes = GsonHolder.getGson().fromJson(jsonData, new TypeToken<List<EducationType>>() {
                }.getType());
                educationTypesList = FXCollections.observableArrayList(educationTypes);
                educationTypesTable.setItems(educationTypesList);
            } else {
                educationTypesList = FXCollections.observableArrayList();
                educationTypesTable.setItems(educationTypesList);
                error("Предупреждение", "Список типов образования пуст.");
            }
        } else {
            error("Ошибка", "Не удалось загрузить типы образования: " + response.getMessage());
        }
    }

    @FXML
    private void addEducationType() {
        String name = nameField.getText().trim();
        String coefficientStr = coefficientField.getText().trim();

        if (name.isEmpty() || coefficientStr.isEmpty()) {
            error("Ошибка", "Название и коэффициент обязательны.");
            return;
        }

        BigDecimal coefficient;
        try {
            coefficient = new BigDecimal(coefficientStr);
            if (coefficient.compareTo(BigDecimal.ZERO) <= 0) {
                error("Ошибка", "Коэффициент должен быть положительным числом.");
                return;
            }
        } catch (NumberFormatException e) {
            error("Ошибка", "Коэффициент должен быть числом.");
            return;
        }

        EducationType newEducationType = new EducationType();
        newEducationType.setName(name);
        newEducationType.setCoefficient(coefficient);
        String data = GsonHolder.getGson().toJson(newEducationType);
        System.out.println("Отправляемый JSON для добавления: " + data);

        Request request = new Request(Operation.ADD_EDUCATION_TYPE, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadEducationTypes();
            clearFields();
        } else {
            error("Ошибка", "Не удалось добавить тип образования: " + response.getMessage());
        }
    }

    @FXML
    private void updateEducationType() {
        EducationType selectedEducationType = educationTypesTable.getSelectionModel().getSelectedItem();
        if (selectedEducationType == null) {
            error("Ошибка", "Выберите тип образования для обновления");
            return;
        }

        String name = nameField.getText().trim();
        String coefficientStr = coefficientField.getText().trim();

        if (name.isEmpty() || coefficientStr.isEmpty()) {
            error("Ошибка", "Название и коэффициент обязательны.");
            return;
        }

        BigDecimal coefficient;
        try {
            coefficient = new BigDecimal(coefficientStr);
            if (coefficient.compareTo(BigDecimal.ZERO) <= 0) {
                error("Ошибка", "Коэффициент должен быть положительным числом.");
                return;
            }
        } catch (NumberFormatException e) {
            error("Ошибка", "Коэффициент должен быть числом.");
            return;
        }

        selectedEducationType.setName(name);
        selectedEducationType.setCoefficient(coefficient);

        String data = GsonHolder.getGson().toJson(selectedEducationType);
        System.out.println("Отправляемый JSON для обновления: " + data);

        Request request = new Request(Operation.UPDATE_EDUCATION_TYPE, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadEducationTypes();
            clearFields();
        } else {
            error("Ошибка", "Не удалось обновить тип образования: " + response.getMessage());
        }
    }

    @FXML
    private void deleteEducationType() {
        EducationType selectedEducationType = educationTypesTable.getSelectionModel().getSelectedItem();
        if (selectedEducationType == null) {
            error("Ошибка", "Выберите тип образования для удаления");
            return;
        }

        ButtonType confirmation = confirmation("Удаление типа образования", "Вы уверены, что хотите удалить тип образования?");
        if (confirmation != ButtonType.OK) {
            return;
        }

        String data = GsonHolder.getGson().toJson(selectedEducationType.getId());
        System.out.println("Отправляемый JSON для удаления: " + data);

        Request request = new Request(Operation.DELETE_EDUCATION_TYPE, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadEducationTypes();
            clearFields();
        } else {
            error("Ошибка", "Не удалось удалить тип образования: " + response.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        coefficientField.clear();
    }

    private void setButtonStates() {
        boolean isSelected = educationTypesTable.getSelectionModel().getSelectedItem() != null;
        updateButton.setDisable(!isSelected);
        deleteButton.setDisable(!isSelected);
    }
}