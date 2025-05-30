package com.example.gui.controllers;

import com.example.gui.enums.StagePath;
import com.example.gui.utils.Loader;
import com.example.server.models.Employee;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
@Setter
public class UserMenuController {


    private static UserMenuController instance;
    @FXML
    private Button logoutButton;
    @FXML
    private VBox dynamicContent;
    private Employee currentEmployee;

    public static UserMenuController getInstance() {
        if (instance == null) {
            instance = new UserMenuController();
        }
        return instance;
    }

    @FXML
    private void initialize() {
        handleViewPayslip();
    }

    private void loadView(StagePath fxmlFile) {
        try {
            System.out.println("Загрузка файла: " + fxmlFile);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile.getPathToFxml()));
            if (loader.getLocation() == null) {
                System.out.println("Ошибка: Файл " + fxmlFile + " не найден");
                return;
            }
            Parent view = loader.load();

            dynamicContent.getChildren().clear();
            dynamicContent.getChildren().add(view);
            System.out.println("Файл успешно загружен в dynamicContent");
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewPayslip() {
        loadView(StagePath.PAY_LIST_USER);
    }

    @FXML
    private void handleViewReports() {
        loadView(StagePath.REPORTS_USER);
    }

    @FXML
    private void handleLogout() {
        Loader.loadScene((Stage) logoutButton.getScene().getWindow(), StagePath.LOGIN);
        System.out.println("Выход выполнен успешно.");
    }
}