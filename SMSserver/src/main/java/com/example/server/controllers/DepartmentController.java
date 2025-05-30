package com.example.server.controllers;

import com.example.server.dao.DepartmentDAO;
import com.example.server.models.Department;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.utils.GsonHolder;

import java.util.List;

public class DepartmentController {
    private final DepartmentDAO departmentDAO;

    public DepartmentController() {
        this.departmentDAO = new DepartmentDAO();
    }

    public Response getAllDepartments() {
        List<Department> departments = departmentDAO.findAll();
        String json = GsonHolder.getGson().toJson(departments);
        return new Response(true, "Отделы загружены", json);
    }

    public Response addDepartment(Request request) {
        String jsonData = request.getData();
        Department department = GsonHolder.getGson().fromJson(jsonData, Department.class);
        departmentDAO.save(department);
        return new Response(true, "Отдел добавлен", null);
    }

    public Response updateDepartment(Request request) {
        String jsonData = request.getData();
        Department department = GsonHolder.getGson().fromJson(jsonData, Department.class);
        departmentDAO.update(department);
        return new Response(true, "Отдел обновлен", null);
    }

    public Response deleteDepartment(Request request) {
        String jsonData = request.getData();
        Integer id = GsonHolder.getGson().fromJson(jsonData, Integer.class);
        Department department = departmentDAO.findById(id);
        if (department != null) {
            departmentDAO.delete(department);
            return new Response(true, "Отдел удален", null);
        }
        return new Response(false, "Отдел не найден", null);
    }
}