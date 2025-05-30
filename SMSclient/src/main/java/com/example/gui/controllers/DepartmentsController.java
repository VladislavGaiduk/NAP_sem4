package com.example.gui.controllers;

import com.example.server.enums.Operation;
import com.example.server.models.Department;
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

public class DepartmentsController {

    @FXML
    private TableView<Department> departmentsTable;
    @FXML
    private TableColumn<Department, Long> idColumn;
    @FXML
    private TableColumn<Department, String> nameColumn;
    @FXML
    private TableColumn<Department, BigDecimal> baseSalaryColumn;

    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField baseSalaryField;

    @FXML
    private Button addDepartmentButton;
    @FXML
    private Button updateDepartmentButton;
    @FXML
    private Button deleteDepartmentButton;

    private ServerClient serverClient;
    private ObservableList<Department> departmentsList;

    @FXML
    private void initialize() {
        System.out.println("DepartmentsController инициализирован в " + java.time.LocalDateTime.now());
        serverClient = ServerClient.getInstance();

        // Настройка столбцов таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        baseSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));


        departmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Загрузка данных
        loadDepartments();

        // Обработчик выбора строки
        departmentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                idField.setText(String.valueOf(newSelection.getId()));
                nameField.setText(newSelection.getName());
                baseSalaryField.setText(newSelection.getBaseSalary().toString());
            } else {
                clearFields();
            }
        });
    }

    private void loadDepartments() {
        Request request = new Request(Operation.GET_ALL_DEPARTMENTS, null);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            String jsonData = response.getData();
            if (jsonData != null && !jsonData.isEmpty()) {
                List<Department> departments = GsonHolder.getGson().fromJson(jsonData, new TypeToken<List<Department>>() {
                }.getType());
                departmentsList = FXCollections.observableArrayList(departments);
                departmentsTable.setItems(departmentsList);
            } else {
                departmentsList = FXCollections.observableArrayList();
                departmentsTable.setItems(departmentsList);
                error("Предупреждение", "Список отделов пуст.");
            }
        } else {
            error("Ошибка", "Не удалось загрузить отделы: " + response.getMessage());
        }
    }

    @FXML
    private void addDepartment() {
        String name = nameField.getText().trim();
        String baseSalaryStr = baseSalaryField.getText().trim();

        if (name.isEmpty() || baseSalaryStr.isEmpty()) {
            error("Ошибка", "Название отдела и базовая зарплата обязательны.");
            return;
        }

        BigDecimal baseSalary;
        try {
            baseSalary = new BigDecimal(baseSalaryStr);
            if (baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
                error("Ошибка", "Базовая зарплата должна быть положительным числом.");
                return;
            }
        } catch (NumberFormatException e) {
            error("Ошибка", "Базовая зарплата должна быть числом.");
            return;
        }

        Department newDepartment = new Department();
        newDepartment.setName(name);
        newDepartment.setBaseSalary(baseSalary);
        String data = GsonHolder.getGson().toJson(newDepartment);
        System.out.println("Отправляемый JSON для добавления: " + data);

        Request request = new Request(Operation.ADD_DEPARTMENT, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadDepartments();
            clearFields();
        } else {
            error("Ошибка", "Не удалось добавить отдел: " + response.getMessage());
        }
    }

    @FXML
    private void updateDepartment() {
        Department selectedDepartment = departmentsTable.getSelectionModel().getSelectedItem();
        if (selectedDepartment == null) {
            error("Ошибка", "Выберите отдел для обновления");
            return;
        }

        String newName = nameField.getText().trim();
        String baseSalaryStr = baseSalaryField.getText().trim();

        if (newName.isEmpty() || baseSalaryStr.isEmpty()) {
            error("Ошибка", "Название отдела и базовая зарплата обязательны.");
            return;
        }

        BigDecimal baseSalary;
        try {
            baseSalary = new BigDecimal(baseSalaryStr);
            if (baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
                error("Ошибка", "Базовая зарплата должна быть положительным числом.");
                return;
            }
        } catch (NumberFormatException e) {
            error("Ошибка", "Базовая зарплата должна быть числом.");
            return;
        }

        selectedDepartment.setName(newName);
        selectedDepartment.setBaseSalary(baseSalary);

        String data = GsonHolder.getGson().toJson(selectedDepartment);
        System.out.println("Отправляемый JSON для обновления: " + data);

        Request request = new Request(Operation.UPDATE_DEPARTMENT, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadDepartments();
            clearFields();
        } else {
            error("Ошибка", "Не удалось обновить отдел: " + response.getMessage());
        }
    }

    @FXML
    private void deleteDepartment() {
        Department selectedDepartment = departmentsTable.getSelectionModel().getSelectedItem();
        if (selectedDepartment == null) {
            error("Ошибка", "Выберите отдел для удаления");
            return;
        }

        ButtonType confirmation = confirmation("Удаление отдела", "Вы уверены, что хотите удалить отдел " + selectedDepartment.getName() + " ?");
        if (confirmation != ButtonType.OK) {
            return;
        }

        String data = GsonHolder.getGson().toJson(selectedDepartment.getId());
        System.out.println("Отправляемый JSON для удаления: " + data);

        Request request = new Request(Operation.DELETE_DEPARTMENT, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadDepartments();
            clearFields();
        } else {
            error("Ошибка", "Не удалось удалить отдел: " + response.getMessage());
        }
    }

    private void clearFields() {
        idField.clear();
        nameField.clear();
        baseSalaryField.clear();
    }
}