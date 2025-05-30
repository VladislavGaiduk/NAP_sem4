package com.example.gui.enums;

import lombok.Getter;

/**
 * Перечисление путей к CSS-файлам для различных тем оформления приложения.
 */
@Getter
public enum ThemesPath {
    LIGHT("/css/light_theme.css"),
    DARK("/css/dark_theme.css"),
    BLUE("/css/blue_theme.css");

    private final String pathToCss;

    ThemesPath(String pathToCss) {
        if (pathToCss == null || pathToCss.trim().isEmpty()) {
            throw new IllegalArgumentException("Путь к CSS-файлу не может быть пустым или null");
        }
        this.pathToCss = pathToCss;
    }
}