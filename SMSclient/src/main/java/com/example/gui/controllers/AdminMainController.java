package com.example.gui.controllers;

import com.example.gui.enums.StagePath;
import com.example.gui.utils.Loader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminMainController {

    private static AdminMainController instance;
    @FXML
    private Button logoutButton;
    @FXML
    private Button menuButton;
    @FXML
    private VBox dynamicContent;
    @FXML
    private BorderPane borderPane;
    @FXML
    private VBox leftPanel;
    private AnchorPane menuPane;
    private boolean isMenuOpen = false;

    public static AdminMainController getInstance() {
        if (instance == null) {
            instance = new AdminMainController();
        }
        return instance;
    }

    @FXML
    private void initialize() {
        handleManageEmployees();
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
    private void handleOpenMenu() {
        if (isMenuOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    private void openMenu() {
        if (menuPane == null) {
            menuPane = new AnchorPane();
            menuPane.setPrefWidth(200);


            Button usersButton = new Button("Управление пользователями");
            usersButton.setOnAction(e -> handleManageUsers());
            usersButton.setLayoutY(10);
            usersButton.setPrefWidth(180);

            Button departmentsButton = new Button("Управление отделами");
            departmentsButton.setOnAction(e -> handleManageDepartments());
            departmentsButton.setLayoutY(50);
            departmentsButton.setPrefWidth(180);

            Button employeesButton = new Button("Управление сотрудниками");
            employeesButton.setOnAction(e -> handleManageEmployees());
            employeesButton.setLayoutY(90);
            employeesButton.setPrefWidth(180);

            Button educationTypesButton = new Button("Управление типами образования");
            educationTypesButton.setOnAction(e -> handleManageEducationTypes());
            educationTypesButton.setLayoutY(130);
            educationTypesButton.setPrefWidth(180);

            Button salariesButton = new Button("Управление зарплатами");
            salariesButton.setOnAction(e -> handleManageSalaries());
            salariesButton.setLayoutY(170);
            salariesButton.setPrefWidth(180);


            Button payListButton = new Button("Создать расчетный лист");
            payListButton.setOnAction(e -> handlecreatePayListButton());
            payListButton.setLayoutY(250);
            payListButton.setPrefWidth(180);

            menuPane.getChildren().addAll(usersButton, departmentsButton, employeesButton, educationTypesButton, salariesButton, payListButton);
        }

        borderPane.setLeft(menuPane);
        isMenuOpen = true;
        menuButton.setText("×");
    }

    private void closeMenu() {
        borderPane.setLeft(leftPanel);
        isMenuOpen = false;
        menuButton.setText("≡");
    }

    @FXML
    private void handleManageUsers() {
        loadView(StagePath.USERS);
        closeMenu();
    }

    @FXML
    private void handleManageDepartments() {
        loadView(StagePath.DEPARTMENTS);
        closeMenu();
    }

    @FXML
    private void handleManageEmployees() {
        loadView(StagePath.EMPLOYEES);
        closeMenu();
    }

    @FXML
    private void handleManageEducationTypes() {
        loadView(StagePath.EDUCATION_TYPES);
        closeMenu();
    }

    @FXML
    private void handleManageSalaries() {
        loadView(StagePath.SALARIES);
        closeMenu();
    }

    @FXML
    private void handleViewReports() {
        loadView(StagePath.REPORTS);
        closeMenu();
    }

    @FXML
    private void handlecreatePayListButton() {
        loadView(StagePath.PAY_LIST);
        closeMenu();
    }

    @FXML
    private void handleLogout() {
        Loader.loadScene((Stage) logoutButton.getScene().getWindow(), StagePath.LOGIN);
        System.out.println("Выход выполнен успешно.");
    }
}