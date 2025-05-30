package com.example.server.controllers;

import com.example.server.dao.UserDAO;
import com.example.server.models.Employee;
import com.example.server.models.User;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.utils.GsonHolder;
import com.example.server.utils.LoginResult;
import org.json.JSONObject;

import java.util.List;

import static com.example.server.utils.DatabaseHandler.hashPassword;

public class UserController {
    private static UserController instance;
    private final UserDAO userDAO;

    public UserController() {
        this.userDAO = new UserDAO();
    }

    public static synchronized UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    public Response getAllUsers() {
        List<User> users = userDAO.findAll();
        String json = GsonHolder.getGson().toJson(users);
        return new Response(true, "Пользователи загружены", json);
    }

    public boolean isExist(String username) {
        return userDAO.findByName(username) != null;
    }

    public Response addUser(Request request) {
        String jsonData = request.getData();
        User user = GsonHolder.getGson().fromJson(jsonData, User.class);
        if (userDAO.findById(user.getId()) != null || EmployeeController.getInstance().isExist(user.getUsername())) {
            return new Response(false, "Такой пользователь уже существует", null);
        }

        userDAO.save(user);
        return new Response(true, "Пользователь добавлен", null);
    }

    public Response updateUser(Request request) {
        String jsonData = request.getData();
        User user = GsonHolder.getGson().fromJson(jsonData, User.class);
        userDAO.update(user);
        return new Response(true, "Пользователь обновлен", null);
    }

    public Response activateUser(Request request) {
        EmployeeController ec = EmployeeController.getInstance();
        String jsonData = request.getData();
        Employee employee = GsonHolder.getGson().fromJson(jsonData, Employee.class);
        String name = employee.getLogin();
        User user = userDAO.findByName(name);
        userDAO.delete(user);
        return ec.addEmployee(request);
    }

    public Response deleteUser(Request request) {
        String jsonData = request.getData();
        Integer id = GsonHolder.getGson().fromJson(jsonData, Integer.class);
        User user = userDAO.findById(id);
        if (user != null) {
            userDAO.delete(user);
            return new Response(true, "Пользователь удален", null);
        }
        return new Response(false, "Пользователь не найден", null);
    }

    public Response login(Request request) {
        try {
            String jsonData = request.getData();
            JSONObject data = new JSONObject(jsonData);
            String username = data.getString("username");
            String password = data.getString("password");

            LoginResult loginResult = userDAO.loginUser(username, password);
            if (loginResult.isSuccess()) {
                String sourceTable = loginResult.getSourceTable();
                JSONObject userData;

                if ("users".equals(sourceTable)) {
                    userData = userDAO.getUserData(username);
                    if (userData != null) {
                        return new Response(false, "Ваша учётная запись ещё не активирована. " +
                                "Дождитесь, пока вашу заявку на регистрацию обработает администратор", null);
                    }
                    return new Response(false, "Данные пользователя не найдены", null);
                } else if ("employees".equals(sourceTable)) {
                    userData = userDAO.getEmployeeData(username);

                    if (userData != null) {


                        return new Response(true, "Успешный вход", loginResult.getData());
                    }
                    return new Response(false, "Данные сотрудника не найдены", null);
                }
            }
            return new Response(false, "Неверные учетные данные", null);
        } catch (Exception e) {
            System.err.println("Ошибка при входе: " + e.getMessage());
            return new Response(false, "Ошибка сервера: " + e.getMessage(), null);
        }
//        return new Response(true, "Успешный вход", null);
    }

    public synchronized Response register(Request request) {
        String jsonData = request.getData();
        JSONObject data = new JSONObject(jsonData);

        String username = data.getString("username");
        String password = hashPassword(data.getString("passwordHash"));
        String firstName = data.getString("firstName");
        String lastName = data.getString("lastName");
        String patronymic = data.getString("patronymic");

        if (userDAO.findByName(username) != null || EmployeeController.getInstance().isExist(username)) {
            return new Response(false, "Такой пользователь уже существует", null);
        }
        if (userDAO.registerUser(username, password, firstName, lastName, patronymic)) {
            return new Response(true, "Регистрация успешна", null);
        }
        return new Response(false, "Регистрация не удалась", null);
    }
}