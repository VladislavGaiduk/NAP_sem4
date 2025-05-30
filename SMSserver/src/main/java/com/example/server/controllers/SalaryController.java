package com.example.server.controllers;

import com.example.server.dao.SalaryDAO;
import com.example.server.models.Salary;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.utils.GsonHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalaryController {
    private final SalaryDAO salaryDAO;

    public SalaryController() {
        this.salaryDAO = new SalaryDAO();
    }

    public Response getAllSalaries() {
        List<Salary> salaries = salaryDAO.findAll();
        String json = GsonHolder.getGson().toJson(salaries);
        return new Response(true, "Зарплаты загружены", json);
    }

    public Response addSalary(Request request) {
        String jsonData = request.getData();
        Salary salary = GsonHolder.getGson().fromJson(jsonData, Salary.class);

        // Проверка на наличие зарплаты в этом месяце
        if (salaryDAO.handleCheckSalaryExists(
                salary.getEmployee().getId() + "," + salary.getPaymentDate().getYear() + "," + salary.getPaymentDate().getMonthValue())) {
            return new Response(false, "У сотрудника может быть только одна зарплата в месяц", null);
        }

        // Расчет зарплаты
        BigDecimal amount = calculateSalaryAmount(salary);
        salary.setAmount(amount);

        salaryDAO.save(salary);
        return new Response(true, "Зарплата добавлена", null);
    }

    public Response updateSalary(Request request) {
        String jsonData = request.getData();
        Salary salary = GsonHolder.getGson().fromJson(jsonData, Salary.class);

        // Проверка на наличие другой зарплаты в этом месяце
        if (salaryDAO.handleCheckOtherSalaryExists(
                salary.getId() + "," + salary.getEmployee().getId() + "," +
                        salary.getPaymentDate().getYear() + "," + salary.getPaymentDate().getMonthValue())) {
            return new Response(false, "У сотрудника может быть только одна зарплата в месяц", null);
        }

        // Расчет зарплаты
        BigDecimal amount = calculateSalaryAmount(salary);
        salary.setAmount(amount);

        salaryDAO.update(salary);
        return new Response(true, "Зарплата обновлена", null);
    }

    private BigDecimal calculateSalaryAmount(Salary salary) {
        // Получение базовой зарплаты и коэффициента
        BigDecimal baseSalary = salary.getEmployee().getDepartment().getBaseSalary();
        BigDecimal coefficient = salary.getEmployee().getEducationType().getCoefficient();

        // Расчет стажа работы
        LocalDate hireDate = salary.getEmployee().getHireDate();
        LocalDate currentDate = LocalDate.now();
        long yearsWorked = ChronoUnit.YEARS.between(hireDate, currentDate);

        // Определение надбавки за стаж
        BigDecimal experienceMultiplier;
        if (yearsWorked >= 15) {
            experienceMultiplier = new BigDecimal("1.5");
        } else if (yearsWorked >= 10) {
            experienceMultiplier = new BigDecimal("1.3");
        } else if (yearsWorked >= 5) {
            experienceMultiplier = new BigDecimal("1.1");
        } else {
            experienceMultiplier = BigDecimal.ONE;
        }

        // Расчет основной зарплаты
        BigDecimal basicSalary = baseSalary.multiply(coefficient).multiply(experienceMultiplier);

        // Расчет больничных дней
        int sickDays = calculateSickDays(salary.getSickLeaveStart(), salary.getSickLeaveEnd());
        int workingDaysInMonth = calculateWorkingDaysInMonth(salary.getPaymentDate());

        // Логика для больничных
        BigDecimal salaryAdjustment = BigDecimal.ZERO;
        if (sickDays >= 10) {
            // Больничный оплачивается, зарплата не меняется
        } else if (sickDays > 0) {
            // Отнимаем пропорциональную часть за пропущенные дни
            BigDecimal dailySalary = basicSalary.divide(new BigDecimal(workingDaysInMonth), 2, RoundingMode.HALF_UP);
            salaryAdjustment = dailySalary.multiply(new BigDecimal(sickDays));
        }

        // Расчет налогов
        BigDecimal taxes = basicSalary.multiply(salary.getTax()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // Итоговая зарплата: основная зарплата - корректировка за больничные - налоги + премия
        return basicSalary.subtract(salaryAdjustment).subtract(taxes).add(salary.getAwardAmount());
    }

    // Метод для расчета рабочих дней в месяце
    private int calculateWorkingDaysInMonth(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        int workingDays = 0;
        for (LocalDate day = firstDay; !day.isAfter(lastDay); day = day.plusDays(1)) {
            if (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
        }
        return workingDays;
    }

    // Метод для расчета количества рабочих дней больничного
    private int calculateSickDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return 0;
        }
        int sickDays = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY && current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                sickDays++;
            }
            current = current.plusDays(1);
        }
        return sickDays;
    }

    public Response CheckOtherSalaryExists(String data) {
        return new Response(salaryDAO.handleCheckOtherSalaryExists(data), null, null);
    }

    public Response CheckSalaryExists(String data) {
        return new Response(salaryDAO.handleCheckSalaryExists(data), null, null);
    }

    private Map<String, String> parseFilterParams(String filterParams) {
        Map<String, String> params = new HashMap<>();
        if (filterParams != null && !filterParams.isEmpty()) {
            String[] pairs = filterParams.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }

    public Response filterSalaries(Request request) {
        String filterParams = request.getData(); // Ожидаем строку вида "employeeId=1,paymentDate=2023-10-15"
        Map<String, String> params = parseFilterParams(filterParams);

        Integer employeeId = params.containsKey("employeeId") ? Integer.parseInt(params.get("employeeId")) : null;
        LocalDate paymentDate = params.containsKey("paymentDate") ? LocalDate.parse(params.get("paymentDate")) : null;

        List<Salary> filteredSalaries = salaryDAO.filterSalaries(employeeId, paymentDate);
        String jsonData = GsonHolder.getGson().toJson(filteredSalaries);
        return new Response(true, "Отфильтрованные зарплаты загружены", jsonData);
    }

    public Response deleteSalary(Request request) {
        String jsonData = request.getData();
        Integer id = GsonHolder.getGson().fromJson(jsonData, Integer.class);
        Salary salary = salaryDAO.findById(id);
        if (salary != null) {
            salaryDAO.delete(salary);
            return new Response(true, "Зарплата удалена", null);
        }
        return new Response(false, "Зарплата не найдена", null);
    }
}