package com.example.server.controllers;

import com.example.server.dao.EducationTypeDAO;
import com.example.server.models.EducationType;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.utils.GsonHolder;

import java.util.List;

public class EducationTypeController {
    private final EducationTypeDAO educationTypeDAO;

    public EducationTypeController() {
        this.educationTypeDAO = new EducationTypeDAO();
    }

    public Response getAllEducationTypes() {
        List<EducationType> educationTypes = educationTypeDAO.findAll();
        String json = GsonHolder.getGson().toJson(educationTypes);
        return new Response(true, "Типы образования загружены", json);
    }

    public Response addEducationType(Request request) {
        String jsonData = request.getData();
        EducationType educationType = GsonHolder.getGson().fromJson(jsonData, EducationType.class);
        educationTypeDAO.save(educationType);
        return new Response(true, "Тип образования добавлен", null);
    }

    public Response updateEducationType(Request request) {
        String jsonData = request.getData();
        EducationType educationType = GsonHolder.getGson().fromJson(jsonData, EducationType.class);
        educationTypeDAO.update(educationType);
        return new Response(true, "Тип образования обновлен", null);
    }

    public Response deleteEducationType(Request request) {
        String jsonData = request.getData();
        Integer id = GsonHolder.getGson().fromJson(jsonData, Integer.class);
        EducationType educationType = educationTypeDAO.findById(id);
        if (educationType != null) {
            educationTypeDAO.delete(educationType);
            return new Response(true, "Тип образования удален", null);
        }
        return new Response(false, "Тип образования не найден", null);
    }
}