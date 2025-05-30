package com.example.gui.controllers;

import com.example.server.enums.Operation;
import com.example.server.models.Employee;
import com.example.server.models.Salary;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.network.ServerClient;
import com.example.server.utils.GsonHolder;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.gui.utils.AlertUtil.error;


public class ReportsControllerUser {

    @FXML
    private LineChart<Number, Number> salaryChart;
    @FXML
    private Button savePdfButton;

    private ServerClient serverClient;
    private List<Salary> salaries;
    private Employee currentEmployee;


    @FXML
    private void initialize() {
        serverClient = ServerClient.getInstance();

        if (currentEmployee == null) {
            currentEmployee = UserMenuController.getInstance().getCurrentEmployee();
        }

        if (currentEmployee == null) {
            error("Ошибка", "Сотрудник не определен.");
            return;
        }

        loadSalaries();
        populateChart();
    }

    private void loadSalaries() {
        if (currentEmployee == null) {
            salaries = List.of();
            error("Ошибка", "Сотрудник не определен, невозможно загрузить зарплаты.");
            return;
        }

        String filterParams = String.format("employeeId=%d", currentEmployee.getId());
        Request request = new Request(Operation.FILTER_SALARIES, filterParams);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            String jsonData = response.getData();
            if (jsonData != null && !jsonData.isEmpty()) {
                salaries = GsonHolder.getGson().fromJson(jsonData, new TypeToken<List<Salary>>() {
                }.getType());
            } else {
                salaries = List.of();
                error("Предупреждение", "Список зарплат пуст для текущего сотрудника.");
            }
        } else {
            salaries = List.of();
            error("Ошибка", "Не удалось загрузить зарплаты: " + response.getMessage());
        }
    }

    private void populateChart() {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Выплаты зарплаты");

        // Группировка зарплат по месяцам
        Map<Integer, BigDecimal> monthlyTotals = salaries.stream()
                .collect(Collectors.groupingBy(
                        salary -> salary.getPaymentDate().getMonthValue(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Salary::getAmount,
                                BigDecimal::add
                        )
                ));

        // Добавление данных в график
        monthlyTotals.forEach((month, total) -> {
            series.getData().add(new XYChart.Data<>(month, total));
        });

        salaryChart.getData().clear();
        salaryChart.getData().add(series);
    }
}