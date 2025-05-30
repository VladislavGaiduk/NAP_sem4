package com.example.gui.controllers;

import com.example.server.enums.Operation;
import com.example.server.models.Employee;
import com.example.server.models.Salary;
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
import java.time.LocalDate;
import java.util.List;

import static com.example.gui.utils.AlertUtil.confirmation;
import static com.example.gui.utils.AlertUtil.error;

public class SalaryController {

    @FXML
    private TableView<Salary> salariesTable;
    @FXML
    private TableColumn<Salary, Integer> idColumn;
    @FXML
    private TableColumn<Salary, String> employeeColumn;
    @FXML
    private TableColumn<Salary, BigDecimal> amountColumn;
    @FXML
    private TableColumn<Salary, LocalDate> paymentDateColumn;
    @FXML
    private TableColumn<Salary, BigDecimal> awardAmountColumn;
    @FXML
    private TableColumn<Salary, String> awardDescriptionColumn;
    @FXML
    private TableColumn<Salary, BigDecimal> taxColumn;

    @FXML
    private ComboBox<Employee> employeeComboBox;
    @FXML
    private TextField amountField;
    @FXML
    private DatePicker paymentDatePicker;
    @FXML
    private TextField awardAmountField;
    @FXML
    private TextField awardDescriptionField;
    @FXML
    private TextField taxField;
    @FXML
    private DatePicker sickLeaveStartPicker;
    @FXML
    private DatePicker sickLeaveEndPicker;

    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    // Новые поля для фильтрации
    @FXML
    private ComboBox<Employee> filterEmployeeComboBox;
    @FXML
    private DatePicker filterPaymentDatePicker;
    @FXML
    private Button filterButton;
    @FXML
    private Button resetFilterButton;

    private ServerClient serverClient;
    private ObservableList<Salary> salariesList;
    private ObservableList<Employee> employeesList;
    private ObservableList<Salary> originalSalariesList; // Для хранения полного списка

    @FXML
    private void initialize() {
        System.out.println("SalaryController инициализирован в " + java.time.LocalDateTime.now());
        serverClient = ServerClient.getInstance();

        // Настройка столбцов таблицы
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        employeeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getEmployee().getFirstName() + " " + cellData.getValue().getEmployee().getLastName()));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        awardAmountColumn.setCellValueFactory(new PropertyValueFactory<>("awardAmount"));
        awardDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("awardDescription"));
        taxColumn.setCellValueFactory(new PropertyValueFactory<>("tax"));

        amountField.setEditable(false);
        salariesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Инициализация списков
        originalSalariesList = FXCollections.observableArrayList();

        // Настройка ComboBox для фильтрации по сотруднику
        filterEmployeeComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                setText(empty || employee == null ? null : employee.getFirstName() + " " + employee.getLastName());
            }
        });
        filterEmployeeComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                setText(empty || employee == null ? null : employee.getFirstName() + " " + employee.getLastName());
            }
        });

        // Настройка ComboBox для формы ввода
        employeeComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                setText(empty || employee == null ? null : employee.getFirstName() + " " + employee.getLastName());
            }
        });
        employeeComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                setText(empty || employee == null ? null : employee.getFirstName() + " " + employee.getLastName());
            }
        });

        // Загрузка данных
        loadEmployees();
        loadSalaries();

        // Обработчик выбора строки
        salariesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                employeeComboBox.setValue(newSelection.getEmployee());
                amountField.setText(newSelection.getAmount().toString());
                paymentDatePicker.setValue(newSelection.getPaymentDate());
                awardAmountField.setText(newSelection.getAwardAmount().toString());
                awardDescriptionField.setText(newSelection.getAwardDescription());
                taxField.setText(newSelection.getTax().toString());
                sickLeaveStartPicker.setValue(newSelection.getSickLeaveStart());
                sickLeaveEndPicker.setValue(newSelection.getSickLeaveEnd());
            } else {
                clearFields();
            }
            setButtonStates();
        });

        // Начальное состояние кнопок
        setButtonStates();
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
                employeeComboBox.setItems(employeesList);
                filterEmployeeComboBox.setItems(employeesList); // Устанавливаем список сотрудников для фильтра
            } else {
                employeesList = FXCollections.observableArrayList();
                employeeComboBox.setItems(employeesList);
                filterEmployeeComboBox.setItems(employeesList);
                error("Предупреждение", "Список сотрудников пуст.");
            }
        } else {
            error("Ошибка", "Не удалось загрузить сотрудников: " + response.getMessage());
        }
    }

    private void loadSalaries() {
        Request request = new Request(Operation.GET_ALL_SALARIES, null);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            String jsonData = response.getData();
            if (jsonData != null && !jsonData.isEmpty()) {
                List<Salary> salaries = GsonHolder.getGson().fromJson(jsonData, new TypeToken<List<Salary>>() {
                }.getType());
                originalSalariesList.setAll(salaries); // Сохраняем полный список
                salariesList = FXCollections.observableArrayList(salaries);
                salariesTable.setItems(salariesList);
            } else {
                originalSalariesList.clear();
                salariesList = FXCollections.observableArrayList();
                salariesTable.setItems(salariesList);
                error("Предупреждение", "Список зарплат пуст.");
            }
        } else {
            error("Ошибка", "Не удалось загрузить зарплаты: " + response.getMessage());
        }
    }

    @FXML
    private void filterSalaries() {
        Employee selectedEmployee = filterEmployeeComboBox.getValue();
        LocalDate selectedDate = filterPaymentDatePicker.getValue();

        // Формируем параметры фильтрации
        String filterParams = "";
        if (selectedEmployee != null) {
            filterParams += "employeeId=" + selectedEmployee.getId();
        }
        if (selectedDate != null) {
            if (!filterParams.isEmpty()) filterParams += ",";
            filterParams += "paymentDate=" + selectedDate;
        }

        // Отправляем запрос на сервер
        Request request = new Request(Operation.FILTER_SALARIES, filterParams);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            String jsonData = response.getData();
            if (jsonData != null && !jsonData.isEmpty()) {
                List<Salary> filteredSalaries = GsonHolder.getGson().fromJson(jsonData, new TypeToken<List<Salary>>() {
                }.getType());
                salariesList.setAll(filteredSalaries);
            } else {
                salariesList.clear();
            }
        } else {
            error("Ошибка", "Не удалось отфильтровать зарплаты: " + response.getMessage());
        }
    }

    @FXML
    private void resetFilter() {
        filterEmployeeComboBox.setValue(null);
        filterPaymentDatePicker.setValue(null);
        salariesList.setAll(originalSalariesList); // Восстанавливаем полный список
    }

    @FXML
    private void addSalary() {
        Employee employee = employeeComboBox.getValue();
        String awardAmountStr = awardAmountField.getText().trim();
        String awardDescription = awardDescriptionField.getText().trim();
        String taxStr = taxField.getText().trim();
        LocalDate paymentDate = paymentDatePicker.getValue();
        LocalDate sickLeaveStart = sickLeaveStartPicker.getValue();
        LocalDate sickLeaveEnd = sickLeaveEndPicker.getValue();

        if (employee == null || paymentDate == null || taxStr.isEmpty()) {
            error("Ошибка", "Работник, налог и дата выплаты обязательны.");
            return;
        }

        BigDecimal awardAmount;
        BigDecimal tax;
        try {
            awardAmount = awardAmountStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(awardAmountStr);
            tax = new BigDecimal(taxStr);
        } catch (NumberFormatException e) {
            error("Ошибка", "Некорректные числовые значения для премии или налогов.");
            return;
        }

        Salary newSalary = new Salary();
        newSalary.setEmployee(employee);
        newSalary.setPaymentDate(paymentDate);
        newSalary.setAwardAmount(awardAmount);
        newSalary.setAwardDescription(awardDescription);
        newSalary.setTax(tax);
        newSalary.setSickLeaveStart(sickLeaveStart);
        newSalary.setSickLeaveEnd(sickLeaveEnd);

        String data = GsonHolder.getGson().toJson(newSalary);
        System.out.println("Отправляемый JSON для добавления: " + data);

        Request request = new Request(Operation.ADD_SALARY, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadSalaries();
            clearFields();
        } else {
            error("Ошибка", "Не удалось добавить зарплату: " + response.getMessage());
        }
    }

    @FXML
    private void updateSalary() {
        Salary selectedSalary = salariesTable.getSelectionModel().getSelectedItem();
        if (selectedSalary == null) {
            error("Ошибка", "Выберите зарплату для обновления.");
            return;
        }

        Employee employee = employeeComboBox.getValue();
        String awardAmountStr = awardAmountField.getText().trim();
        String awardDescription = awardDescriptionField.getText().trim();
        String taxStr = taxField.getText().trim();
        LocalDate paymentDate = paymentDatePicker.getValue();
        LocalDate sickLeaveStart = sickLeaveStartPicker.getValue();
        LocalDate sickLeaveEnd = sickLeaveEndPicker.getValue();

        if (employee == null || paymentDate == null || taxStr.isEmpty()) {
            error("Ошибка", "Работник, налог и дата выплаты обязательны.");
            return;
        }

        BigDecimal awardAmount;
        BigDecimal tax;
        try {
            awardAmount = awardAmountStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(awardAmountStr);
            tax = new BigDecimal(taxStr);
        } catch (NumberFormatException e) {
            error("Ошибка", "Некорректные числовые значения для премии или налогов.");
            return;
        }

        selectedSalary.setEmployee(employee);
        selectedSalary.setPaymentDate(paymentDate);
        selectedSalary.setAwardAmount(awardAmount);
        selectedSalary.setAwardDescription(awardDescription);
        selectedSalary.setTax(tax);
        selectedSalary.setSickLeaveStart(sickLeaveStart);
        selectedSalary.setSickLeaveEnd(sickLeaveEnd);

        String data = GsonHolder.getGson().toJson(selectedSalary);
        System.out.println("Отправляемый JSON для обновления: " + data);

        Request request = new Request(Operation.UPDATE_SALARY, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadSalaries();
            clearFields();
        } else {
            error("Ошибка", "Не удалось обновить зарплату: " + response.getMessage());
        }
    }

    private boolean isOtherSalaryExistsForMonth(Integer excludeSalaryId, Integer employeeId, int year, int month) {
        String data = excludeSalaryId + "," + employeeId + "," + year + "," + month;
        Request request = new Request(Operation.CHECK_OTHER_SALARY_EXISTS, data);
        Response response = serverClient.sendRequest(request);
        return response.isSuccess();
    }

    private boolean isSalaryExistsForMonth(Integer employeeId, int year, int month) {
        String data = employeeId + "," + year + "," + month;
        Request request = new Request(Operation.CHECK_SALARY_EXISTS, data);
        Response response = serverClient.sendRequest(request);
        return response.isSuccess();
    }

    @FXML
    private void deleteSalary() {
        Salary selectedSalary = salariesTable.getSelectionModel().getSelectedItem();
        if (selectedSalary == null) {
            error("Ошибка", "Выберите зарплату для удаления");
            return;
        }

        ButtonType confirmation = confirmation("Удаление зарплаты", "Вы уверены, что хотите удалить зарплату?");
        if (confirmation != ButtonType.OK) {
            return;
        }

        String data = GsonHolder.getGson().toJson(selectedSalary.getId());
        System.out.println("Отправляемый JSON для удаления: " + data);

        Request request = new Request(Operation.DELETE_SALARY, data);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            loadSalaries();
            clearFields();
        } else {
            error("Ошибка", "Не удалось удалить зарплату: " + response.getMessage());
        }
    }

    private void clearFields() {
        employeeComboBox.setValue(null);
        amountField.clear();
        paymentDatePicker.setValue(null);
        awardAmountField.clear();
        awardDescriptionField.clear();
        taxField.clear();
        sickLeaveEndPicker.setValue(null);
        sickLeaveStartPicker.setValue(null);
    }

    private void setButtonStates() {
        boolean isSelected = salariesTable.getSelectionModel().getSelectedItem() != null;
        updateButton.setDisable(!isSelected);
        deleteButton.setDisable(!isSelected);
    }
}