package com.example.gui.controllers;

import com.example.server.enums.Operation;
import com.example.server.models.Department;
import com.example.server.models.EducationType;
import com.example.server.models.Employee;
import com.example.server.models.User;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.network.ServerClient;
import com.example.server.utils.GsonHolder;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

import static com.example.gui.utils.AlertUtil.error;

public class ActivationWindowController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField patronymicField;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private DatePicker hireDatePicker;
    @FXML
    private ComboBox<Department> departmentComboBox;
    @FXML
    private ComboBox<EducationType> educationTypeComboBox;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    private ServerClient serverClient;
    private User selectedUser;
    @Setter
    @Getter
    private Stage stage;

    @FXML
    private void initialize() {
        serverClient = ServerClient.getInstance();
        loadDepartments();
        loadEducationTypes();

        // Настройка отображения названий в ComboBox
        departmentComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Department department, boolean empty) {
                super.updateItem(department, empty);
                setText(empty || department == null ? null : department.getName());
            }
        });
        departmentComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Department department, boolean empty) {
                super.updateItem(department, empty);
                setText(empty || department == null ? null : department.getName());
            }
        });

        educationTypeComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(EducationType educationType, boolean empty) {
                super.updateItem(educationType, empty);
                setText(empty || educationType == null ? null : educationType.getName());
            }
        });
        educationTypeComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(EducationType educationType, boolean empty) {
                super.updateItem(educationType, empty);
                setText(empty || educationType == null ? null : educationType.getName());
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
                departmentComboBox.getItems().setAll(departments);
            } else {
                departmentComboBox.getItems().clear();
                error("Предупреждение", "Список отделов пуст.");
            }
        } else {
            error("Ошибка", "Не удалось загрузить отделы: " + response.getMessage());
        }
    }

    private void loadEducationTypes() {
        Request request = new Request(Operation.GET_ALL_EDUCATION_TYPES, null);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            String jsonData = response.getData();
            if (jsonData != null && !jsonData.isEmpty()) {
                List<EducationType> educationTypes = GsonHolder.getGson().fromJson(jsonData, new TypeToken<List<EducationType>>() {
                }.getType());
                educationTypeComboBox.getItems().setAll(educationTypes);
            } else {
                educationTypeComboBox.getItems().clear();
                error("Предупреждение", "Список типов образования пуст.");
            }
        } else {
            error("Ошибка", "Не удалось загрузить типы образования: " + response.getMessage());
        }
    }

    public void setSelectedUser(User user) {
        this.selectedUser = user;
        if (user != null) {
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            patronymicField.setText(user.getPatronymic());
            loginField.setText(user.getUsername());
        }
    }

    @FXML
    private void handleConfirm() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String patronymic = patronymicField.getText().trim();
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        LocalDate hireDate = hireDatePicker.getValue();
        Department department = departmentComboBox.getValue();
        EducationType educationType = educationTypeComboBox.getValue();

        if (firstName.isEmpty() || lastName.isEmpty() || login.isEmpty() || hireDate == null || department == null || educationType == null) {
            error("Ошибка", "Все поля, кроме отчества, обязательны.");
            return;
        }

        Employee newEmployee = new Employee();
        //newEmployee.setId(selectedUser.getId());
        newEmployee.setFirstName(firstName);
        newEmployee.setLastName(lastName);
        newEmployee.setPatronymic(patronymic.isEmpty() ? null : patronymic);
        newEmployee.setLogin(login);
        newEmployee.setPasswordHash(password);
        newEmployee.setHireDate(hireDate);
        newEmployee.setDepartment(department);
        newEmployee.setEducationType(educationType);

        String data = GsonHolder.getGson().toJson(newEmployee);

        System.out.println("Отправляемый JSON для переноса: " + data);

        Request request = new Request(Operation.TRANSFER_USER_TO_EMPLOYEE, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            stage.close();
        } else {
            error("Ошибка", "Не удалось перенести пользователя: " + response.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }
}