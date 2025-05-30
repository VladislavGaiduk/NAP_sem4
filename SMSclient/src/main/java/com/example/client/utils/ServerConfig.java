package com.example.client.utils;

import java.io.*;
import java.util.Properties;

public class ServerConfig {
    private static final String CONFIG_FILE_NAME = "server.properties";
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".server-config";
    private static final String CONFIG_FILE_PATH = CONFIG_DIR + File.separator + CONFIG_FILE_NAME;
    private static final Properties properties = new Properties();
    private static final String DEFAULT_IP = "localhost";
    private static final int DEFAULT_PORT = 6666;
    private static String theme;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        // Сначала пытаемся загрузить файл из пользовательской директории
        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
                return;
            } catch (IOException e) {
                System.err.println("Ошибка загрузки конфигурации из " + CONFIG_FILE_PATH + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Если файл в пользовательской директории отсутствует, загружаем из resources
        try (InputStream inputStream = ServerConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                // Если файл отсутствует в resources, создаём с настройками по умолчанию
                properties.setProperty("ip", DEFAULT_IP);
                properties.setProperty("port", String.valueOf(DEFAULT_PORT));
                saveConfig(); // Сохраняем в пользовательскую директорию
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки конфигурации из resources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        // Создаём директорию, если она не существует
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        // Сохраняем настройки в пользовательскую директорию
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE_PATH)) {
            properties.store(fos, "Server Configuration");
            System.out.println("Конфигурация сохранена в " + CONFIG_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения конфигурации в " + CONFIG_FILE_PATH + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getIp() {
        return properties.getProperty("ip", DEFAULT_IP);
    }

    public static void setIp(String ip) {
        properties.setProperty("ip", ip);
    }

    public static int getPort() {
        return Integer.parseInt(properties.getProperty("port", String.valueOf(DEFAULT_PORT)));
    }

    public static void setPort(int port) {
        properties.setProperty("port", String.valueOf(port));
    }
}