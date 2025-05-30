package com.example.server.controllers;

import com.example.server.dao.EmployeeDAO;
import com.example.server.models.Employee;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.utils.GsonHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeController {
    private static EmployeeController instance;
    private final EmployeeDAO employeeDAO;

    private EmployeeController() {
        this.employeeDAO = new EmployeeDAO();
    }

    public static synchronized EmployeeController getInstance() {
        if (instance == null) {
            instance = new EmployeeController();
        }
        return instance;
    }

    public boolean isExist(String login) {
        return employeeDAO.findByLogin(login) != null;
    }

    public Response getAllEmployees() {
        List<Employee> employees = employeeDAO.findAll();
        String json = GsonHolder.getGson().toJson(employees);
        return new Response(true, "Работники загружены", json);
    }

    public Response addEmployee(Request request) {
        String jsonData = request.getData();
        Employee employee = GsonHolder.getGson().fromJson(jsonData, Employee.class);
        if (UserController.getInstance().isExist(employee.getLogin()) || isExist(employee.getLogin())) {
            return new Response(false, "Такой пользователь уже существует", null);
        }
        employeeDAO.save(employee);
        return new Response(true, "Работник добавлен", null);
    }

    public Response updateEmployee(Request request) {
        String jsonData = request.getData();
        Employee employee = GsonHolder.getGson().fromJson(jsonData, Employee.class);
        employeeDAO.update(employee);
        return new Response(true, "Работник обновлен", null);
    }

    public Response deleteEmployee(Request request) {
        String jsonData = request.getData();
        Integer id = GsonHolder.getGson().fromJson(jsonData, Integer.class);
        Employee employee = employeeDAO.findById(id);
        if (employee != null) {
            employeeDAO.delete(employee);
            return new Response(true, "Работник удален", null);
        }
        return new Response(false, "Работник не найден", null);
    }

    public Response searchEmployees(Request request) {
        String searchParams = request.getData(); // Ожидаем строку вида "firstName=Иван,lastName=Иванов,departmentName=IT"
        Map<String, String> params = parseSearchParams(searchParams);

        String firstName = params.get("firstName");
        String lastName = params.get("lastName");
        String departmentName = params.get("departmentName");

        List<Employee> employees = employeeDAO.searchEmployees(firstName, lastName, departmentName);
        String jsonData = GsonHolder.getGson().toJson(employees);
        return new Response(true, "Сотрудники найдены", jsonData);
    }

    private Map<String, String> parseSearchParams(String searchParams) {
        Map<String, String> params = new HashMap<>();
        if (searchParams != null && !searchParams.isEmpty()) {
            String[] pairs = searchParams.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
}