package com.example.server.network;

import com.example.server.controllers.*;
import com.example.server.enums.Operation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientThread implements Runnable {

    private final Socket clientSocket;
    private final AtomicInteger clientCount;
    private final UserController userController;
    private final DepartmentController departmentController;
    private final EmployeeController employeeController;
    private final EducationTypeController educationTypeController;
    private final SalaryController salaryController;

    public ClientThread(Socket clientSocket, AtomicInteger clientCount) {
        this.clientSocket = clientSocket;
        this.clientCount = clientCount;
        this.userController = new UserController();
        this.departmentController = new DepartmentController();
        this.employeeController = EmployeeController.getInstance();
        this.educationTypeController = new EducationTypeController();
        this.salaryController = new SalaryController();
    }

    @Override
    public void run() {
        try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {

            boolean keepRunning = true;
            while (keepRunning) {
                Request request = (Request) input.readObject();
                if (request != null) {
                    Response response = processRequest(request);
                    if (request.getOperation() == Operation.DISCONNECT) {
                        keepRunning = false;
                    }
                    output.writeObject(response);
                    output.flush();
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка обработки клиента: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private Response processRequest(Request request) {
        try {
            return switch (request.getOperation()) {
                // Пользователи
                case GET_ALL_USERS -> userController.getAllUsers();
                case ADD_USER -> userController.addUser(request);
                case UPDATE_USER -> userController.updateUser(request);
                case DELETE_USER -> userController.deleteUser(request);
                case LOGIN -> userController.login(request);
                case REGISTER -> userController.register(request);

                case TRANSFER_USER_TO_EMPLOYEE -> userController.activateUser(request);


                // Отделы
                case GET_ALL_DEPARTMENTS -> departmentController.getAllDepartments();
                case ADD_DEPARTMENT -> departmentController.addDepartment(request);
                case UPDATE_DEPARTMENT -> departmentController.updateDepartment(request);
                case DELETE_DEPARTMENT -> departmentController.deleteDepartment(request);

                // Работники
                case GET_ALL_EMPLOYEES -> employeeController.getAllEmployees();
                case ADD_EMPLOYEE -> employeeController.addEmployee(request);
                case UPDATE_EMPLOYEE -> employeeController.updateEmployee(request);
                case DELETE_EMPLOYEE -> employeeController.deleteEmployee(request);
                case SEARCH_EMPLOYEES -> employeeController.searchEmployees(request);

                // Типы образования
                case GET_ALL_EDUCATION_TYPES -> educationTypeController.getAllEducationTypes();
                case ADD_EDUCATION_TYPE -> educationTypeController.addEducationType(request);
                case UPDATE_EDUCATION_TYPE -> educationTypeController.updateEducationType(request);
                case DELETE_EDUCATION_TYPE -> educationTypeController.deleteEducationType(request);

                // Зарплаты
                case GET_ALL_SALARIES -> salaryController.getAllSalaries();
                case ADD_SALARY -> salaryController.addSalary(request);
                case UPDATE_SALARY -> salaryController.updateSalary(request);
                case DELETE_SALARY -> salaryController.deleteSalary(request);
                case CHECK_OTHER_SALARY_EXISTS -> salaryController.CheckOtherSalaryExists(request.getData());
                case CHECK_SALARY_EXISTS -> salaryController.CheckSalaryExists(request.getData());
                case FILTER_SALARIES -> salaryController.filterSalaries(request);

                case DISCONNECT -> new Response(true, "Отключение успешно", null);
                default -> new Response(false, "Неизвестная операция", null);
            };
        } catch (Exception e) {
            return new Response(false, "Ошибка на сервере: " + e.getMessage(), null);
        }
    }

    private synchronized void closeConnection() {
        try {
            clientSocket.close();
            int currentCount = clientCount.decrementAndGet();
            System.out.println("Соединение потеряно. Количество клиентов: " + currentCount);
        } catch (IOException e) {
            System.err.println("Ошибка закрытия соединения: " + e.getMessage());
        }
    }
}