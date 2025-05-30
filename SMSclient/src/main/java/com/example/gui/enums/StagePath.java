package com.example.gui.enums;

import lombok.Getter;

@Getter
public enum StagePath {
    LOGIN("/views/login.fxml"),
    REGISTRATION("/views/register.fxml"),
    MENU_ADMIN("/views/admin_main.fxml"),
    MENU_USER("/views/user_menu.fxml"),
    USERS("/views/users_managment.fxml"),
    ACTIVATION_USER("/views/activation_window.fxml"),
    USER_TRANSFERING("/views/user_transfering.fxml"),
    EDUCATION_TYPES("/views/education_types_managment.fxml"),
    SALARIES("/views/salaries_managment.fxml"),
    DEPARTMENTS("/views/departments_managment.fxml"),
    EMPLOYEES("/views/employees_managment.fxml"),
    CONNECTION_CHECK("/views/connectionCheck.fxml"),
    REPORTS("/views/reports.fxml"),
    REPORTS_USER("/views/reports_user.fxml"),
    PAY_LIST("/views/payslip.fxml"),
    PAY_LIST_USER("/views/payslip_user.fxml");

    private final String pathToFxml;

    StagePath(String pathToFxml) {
        this.pathToFxml = pathToFxml;
    }
}

