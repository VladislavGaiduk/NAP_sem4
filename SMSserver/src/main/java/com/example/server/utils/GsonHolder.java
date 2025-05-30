package com.example.server.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GsonHolder {
    @Getter
    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            // Регистрируем адаптеры для LocalDate и LocalDateTime
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            // Настроим политику именования полей
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .create();

    // Приватный конструктор, чтобы избежать создания экземпляра класса
    private GsonHolder() {
    }
}
