package com.example.gui.controllers;

import com.example.server.enums.Operation;
import com.example.server.models.Department;
import com.example.server.models.EducationType;
import com.example.server.models.Employee;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.network.ServerClient;
import com.example.server.utils.GsonHolder;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

import static com.example.gui.utils.AlertUtil.confirmation;
import static com.example.gui.utils.AlertUtil.error;

public class EmployeesController {

    @FXML
    private TableView<Employee> employeesTable;
    @FXML
    private TableColumn<Employee, Long> idColumn;
    @FXML
    private TableColumn<Employee, String> firstNameColumn;
    @FXML
    private TableColumn<Employee, String> lastNameColumn;
    @FXML
    private TableColumn<Employee, String> patronymicColumn;
    @FXML
    private TableColumn<Employee, String> loginColumn;
    @FXML
    private TableColumn<Employee, LocalDate> hireDateColumn;
    @FXML
    private TableColumn<Employee, String> departmentColumn;
    @FXML
    private TableColumn<Employee, String> educationTypeColumn;

    @FXML
    private TextField searchFirstNameField;
    @FXML
    private TextField searchLastNameField;
    @FXML
    private TextField searchDepartmentField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField patronymicField;
    @FXML
    private TextField loginField;
    @FXML
    private TextField passwordField;
    @FXML
    private DatePicker hireDatePicker;
    @FXML
    private ComboBox<Department> departmentComboBox;
    @FXML
    private ComboBox<EducationType> educationTypeComboBox;


    @FXML
    private Button searchButton;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private ServerClient serverClient;
    private ObservableList<Employee> employeesList;
    private ObservableList<Department> departmentsList;
    private ObservableList<EducationType> educationTypesList;

    @FXML
    private void initialize() {
        System.out.println("EmployeesController инициализирован в " + java.time.LocalDateTime.now());
        serverClient = ServerClient.getInstance();

        employeesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Настройка простых столбцов
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        patronymicColumn.setCellValueFactory(new PropertyValueFactory<>("patronymic"));
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        hireDateColumn.setCellValueFactory(new PropertyValueFactory<>("hireDate"));

        // Настройка столбца для имени отдела
        departmentColumn.setCellValueFactory(cellData -> {
            Employee employee = cellData.getValue();
            if (employee.getDepartment() != null) {
                return new SimpleStringProperty(employee.getDepartment().getName());
            } else {
                return new SimpleStringProperty("Не указан");
            }
        });

        // Настройка столбца для названия образования
        educationTypeColumn.setCellValueFactory(cellData -> {
            Employee employee = cellData.getValue();
            if (employee.getEducationType() != null) {
                return new SimpleStringProperty(employee.getEducationType().getName());
            } else {
                return new SimpleStringProperty("Не указан");
            }
        });


        // Загрузка данных
        loadDepartments();
        loadEducationTypes();
        loadEmployees();

        // Настройка ComboBox для отображения имени отдела
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

        // Настройка ComboBox для отображения имени типа образования
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
        employeesTable.refresh();
        // Обработчик выбора строки
        employeesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                firstNameField.setText(newSelection.getFirstName());
                lastNameField.setText(newSelection.getLastName());
                patronymicField.setText(newSelection.getPatronymic());
                loginField.setText(newSelection.getLogin());
                passwordField.setText(""); // Очищаем поле пароля
                hireDatePicker.setValue(newSelection.getHireDate());
                departmentComboBox.setValue(newSelection.getDepartment());
                educationTypeComboBox.setValue(newSelection.getEducationType());
                setButtonStates();
            } else {
                clearFields();
                setButtonStates();
            }
        });

        // Начальное состояние кнопок
        setButtonStates();
        employeesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
                departmentComboBox.setItems(departmentsList);
            } else {
                departmentsList = FXCollections.observableArrayList();
                departmentComboBox.setItems(departmentsList);
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
                educationTypesList = FXCollections.observableArrayList(educationTypes);
                educationTypeComboBox.setItems(educationTypesList);
            } else {
                educationTypesList = FXCollections.observableArrayList();
                educationTypeComboBox.setItems(educationTypesList);
                error("Предупреждение", "Список типов образования пуст.");
            }
        } else {
            error("Ошибка", "Не удалось загрузить типы образования: " + response.getMessage());
        }
    }

    private void loadEmployees() {
        Request request = new Request(Operation.GET_ALL_EMPLOYEES, null);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            String jsonData = response.getData();
            if (jsonData != null && !jsonData.isEmpty()) {
                List<Employee> employees = GsonHolder.getGson().fromJson(jsonData, new TypeToken<List<Employee>>() {
                }.getType());
                employeesList = FXCollections.observableArrayList(employees);
                employeesTable.setItems(employeesList);
            } else {
                employeesList = FXCollections.observableArrayList();
                employeesTable.setItems(employeesList);
                error("Предупреждение", "Список работников пуст.");
            }
        } else {
            error("Ошибка", "Не удалось загрузить работников: " + response.getMessage());
        }
    }

    @FXML
    private void addEmployee() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String patronymic = patronymicField.getText().trim();
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        LocalDate hireDate = hireDatePicker.getValue();
        Department department = departmentComboBox.getValue();
        EducationType educationType = educationTypeComboBox.getValue();

        if (firstName.isEmpty() || lastName.isEmpty() || login.isEmpty() || password.isEmpty() || hireDate == null || department == null || educationType == null) {
            error("Ошибка", "Все поля, кроме отчества, обязательны.");
            return;
        }

        Employee newEmployee = new Employee();
        newEmployee.setFirstName(firstName);
        newEmployee.setLastName(lastName);
        newEmployee.setPatronymic(patronymic.isEmpty() ? null : patronymic);
        newEmployee.setLogin(login);
        newEmployee.setPasswordHash(password);
        newEmployee.setHireDate(hireDate);
        newEmployee.setDepartment(department);
        newEmployee.setEducationType(educationType);

        String data = GsonHolder.getGson().toJson(newEmployee);
        System.out.println("Отправляемый JSON для добавления: " + data);

        Request request = new Request(Operation.ADD_EMPLOYEE, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadEmployees();
            clearFields();
        } else {
            error("Ошибка", "Не удалось добавить работника: " + response.getMessage());
        }
    }

    @FXML
    private void updateEmployee() {
        Employee selectedEmployee = employeesTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee == null) {
            error("Ошибка", "Выберите работника для обновления.");
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String patronymic = patronymicField.getText().trim();
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        LocalDate hireDate = hireDatePicker.getValue();
        Department department = departmentComboBox.getValue();
        EducationType educationType = educationTypeComboBox.getValue();

        if (firstName.isEmpty() || lastName.isEmpty() || login.isEmpty() || hireDate == null || department == null || educationType == null) {
            error("Ошибка", "Все поля, кроме отчества и пароля, обязательны.");
            return;
        }

        selectedEmployee.setFirstName(firstName);
        selectedEmployee.setLastName(lastName);
        selectedEmployee.setPatronymic(patronymic.isEmpty() ? null : patronymic);
        selectedEmployee.setLogin(login);
        if (!password.isEmpty()) {
            selectedEmployee.setPasswordHash(password);
        }
        selectedEmployee.setHireDate(hireDate);
        selectedEmployee.setDepartment(department);
        selectedEmployee.setEducationType(educationType);

        String data = GsonHolder.getGson().toJson(selectedEmployee);
        System.out.println("Отправляемый JSON для обновления: " + data);

        Request request = new Request(Operation.UPDATE_EMPLOYEE, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            clearFields();
            loadEmployees();

        } else {
            error("Ошибка", "Не удалось обновить работника: " + response.getMessage());
        }
    }

    @FXML
    private void searchEmployees() {
        String firstName = searchFirstNameField.getText().trim();
        String lastName = searchLastNameField.getText().trim();
        String departmentName = searchDepartmentField.getText().trim();

        // Формируем параметры поиска
        String searchParams = "";
        if (!firstName.isEmpty()) {
            searchParams += "firstName=" + firstName;
        }
        if (!lastName.isEmpty()) {
            if (!searchParams.isEmpty()) searchParams += ",";
            searchParams += "lastName=" + lastName;
        }
        if (!departmentName.isEmpty()) {
            if (!searchParams.isEmpty()) searchParams += ",";
            searchParams += "departmentName=" + departmentName;
        }

        // Отправляем запрос на сервер
        Request request = new Request(Operation.SEARCH_EMPLOYEES, searchParams);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            String jsonData = response.getData();
            if (jsonData != null && !jsonData.isEmpty()) {
                List<Employee> employees = GsonHolder.getGson().fromJson(jsonData, new TypeToken<List<Employee>>() {
                }.getType());
                employeesList.setAll(employees);
            } else {
                employeesList.clear();
            }
        } else {
            error("Ошибка", "Не удалось выполнить поиск: " + response.getMessage());
        }
    }


    @FXML
    private void resetSearch() {
        searchFirstNameField.clear();
        searchLastNameField.clear();
        searchDepartmentField.clear();
        loadDepartments();
        loadEducationTypes();
        loadEmployees();
    }

    @FXML
    private void deleteEmployee() {
        Employee selectedEmployee = employeesTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee == null) {
            error("Ошибка", "Выберите работника для удаления.");
            return;
        }

        ButtonType confirmation = confirmation("Удаление работника", "Вы уверены, что хотите удалить работника " +
                selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName() + "?");
        if (confirmation != ButtonType.OK) {
            return;
        }


        String data = GsonHolder.getGson().toJson(selectedEmployee.getId());
        System.out.println("Отправляемый JSON для удаления: " + data);

        Request request = new Request(Operation.DELETE_EMPLOYEE, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadEmployees();
            clearFields();
        } else {
            error("Ошибка", "Не удалось удалить работника: " + response.getMessage());
        }
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        patronymicField.clear();
        loginField.clear();
        passwordField.clear();
        hireDatePicker.setValue(null);
        departmentComboBox.setValue(null);
        educationTypeComboBox.setValue(null);
    }

    private void setButtonStates() {
        boolean isSelected = employeesTable.getSelectionModel().getSelectedItem() != null;
        updateButton.setDisable(!isSelected);
        deleteButton.setDisable(!isSelected);
    }
}