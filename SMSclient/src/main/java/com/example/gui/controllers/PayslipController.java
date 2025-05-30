package com.example.gui.controllers;

import com.example.server.enums.Operation;
import com.example.server.models.Employee;
import com.example.server.models.Salary;
import com.example.server.network.Request;
import com.example.server.network.Response;
import com.example.server.network.ServerClient;
import com.example.server.utils.GsonHolder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.gui.utils.AlertUtil.error;
import static com.example.gui.utils.AlertUtil.info;

public class PayslipController {

    @FXML
    private ComboBox<Employee> employeeComboBox;
    @FXML
    private ComboBox<LocalDate> paymentDateComboBox;
    @FXML
    private GridPane payslipGrid;
    @FXML
    private Label fullNameText;
    @FXML
    private Label departmentText;
    @FXML
    private Label baseSalaryText;
    @FXML
    private Label educationTypeText;
    @FXML
    private Label coefficientText;
    @FXML
    private Label paymentDateText;
    @FXML
    private Label amountText;
    @FXML
    private Label awardAmountText;
    @FXML
    private Label awardDescriptionText;
    @FXML
    private Label taxText;
    @FXML
    private Label sickDaysText;
    @FXML
    private Label calculationText;
    @FXML
    private Button savePdfButton;

    private ServerClient serverClient;
    private ObservableList<Employee> employees;
    private ObservableList<LocalDate> paymentDates;
    private Salary selectedSalary;
    private Employee selectedEmployee;

    @FXML
    private void initialize() {
        serverClient = ServerClient.getInstance();
        paymentDates = FXCollections.observableArrayList();
        paymentDateComboBox.setItems(paymentDates);
        loadEmployees();
        setupEmployeeComboBox();
        setupPaymentDateComboBox();
        updatePayslipData();
    }

    private void loadEmployees() {
        Request request = new Request(Operation.GET_ALL_EMPLOYEES, null);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            String jsonData = response.getData();
            if (jsonData != null && !jsonData.isEmpty()) {
                List<Employee> employeeList = GsonHolder.getGson().fromJson(jsonData, new TypeToken<List<Employee>>() {
                }.getType());
                employees = FXCollections.observableArrayList(employeeList);
                employeeComboBox.setItems(employees);
            } else {
                employees = FXCollections.observableArrayList();
                error("Предупреждение", "Список сотрудников пуст.");
            }
        } else {
            error("Ошибка", "Не удалось загрузить сотрудников: " + response.getMessage());
        }
    }

    private void setupEmployeeComboBox() {
        employeeComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                setText(empty || employee == null ? null :
                        String.format("%s %s", employee.getLastName(), employee.getFirstName()));
            }
        });
        employeeComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                setText(empty || employee == null ? null :
                        String.format("%s %s", employee.getLastName(), employee.getFirstName()));
            }
        });
        employeeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
            selectedEmployee = newValue;
            paymentDateComboBox.getSelectionModel().clearSelection();
            updatePaymentDates();
            updatePayslipData();
        });
    }

    private void setupPaymentDateComboBox() {
        paymentDateComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.toString());
            }
        });
        paymentDateComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.toString());
            }
        });
        paymentDateComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
            System.out.println("Выбрана дата: " + newValue);
            updatePayslipData();
        });
    }

    private void updatePaymentDates() {
        paymentDates.clear();
        if (selectedEmployee == null) {
            System.out.println("Сотрудник не выбран, даты не загружаются.");
            return;
        }

        String filterParams = String.format("employeeId=%d", selectedEmployee.getId());
        System.out.println("Параметры запроса для дат: " + filterParams);
        Request request = new Request(Operation.FILTER_SALARIES, filterParams);
        Response response = serverClient.sendRequest(request);
        if (response.isSuccess()) {
            String jsonData = response.getData();
            System.out.println("Ответ сервера для дат: " + jsonData);
            if (jsonData != null && !jsonData.isEmpty()) {
                List<Salary> salaries = GsonHolder.getGson().fromJson(jsonData, new TypeToken<List<Salary>>() {
                }.getType());
                List<LocalDate> dates = salaries.stream()
                        .map(Salary::getPaymentDate)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
                paymentDates.setAll(dates);
                System.out.println("Загружены даты: " + dates);
            } else {
                System.out.println("Список зарплат пуст для сотрудника ID: " + selectedEmployee.getId());
            }
        } else {
            error("Ошибка", "Не удалось загрузить даты выплат: " + response.getMessage());
            System.out.println("Ошибка сервера: " + response.getMessage());
        }
    }

    private void updatePayslipData() {
        Employee employee = employeeComboBox.getValue();
        LocalDate paymentDate = paymentDateComboBox.getValue();

        System.out.println("Обновление данных: сотрудник=" + (employee != null ? employee.getId() : "null") + ", дата=" + paymentDate);

        if (employee == null || paymentDate == null) {
            System.out.println("Сотрудник или дата не выбраны, очистка данных.");
            clearPayslipData();
            return;
        }

        String filterParams = String.format("employeeId=%d,paymentDate=%s", employee.getId(), paymentDate);
        System.out.println("Параметры запроса для зарплаты: " + filterParams);
        Request request = new Request(Operation.FILTER_SALARIES, filterParams);
        Response response = serverClient.sendRequest(request);

        if (response.isSuccess()) {
            String jsonData = response.getData();
            System.out.println("Ответ сервера для зарплат: " + jsonData);
            if (jsonData != null && !jsonData.isEmpty()) {
                List<Salary> salaries = GsonHolder.getGson().fromJson(jsonData, new TypeToken<List<Salary>>() {
                }.getType());
                selectedSalary = salaries.isEmpty() ? null : salaries.get(0);
                System.out.println("Выбрана зарплата: " + (selectedSalary != null ? selectedSalary.getId() : "null"));
            } else {
                selectedSalary = null;
                System.out.println("Список зарплат пуст для параметров: " + filterParams);
                error("Предупреждение", "Нет данных о зарплате для выбранной даты и сотрудника.");
            }
        } else {
            selectedSalary = null;
            error("Ошибка", "Не удалось загрузить данные зарплаты: " + response.getMessage());
            System.out.println("Ошибка сервера: " + response.getMessage());
        }

        if (selectedSalary != null && selectedEmployee != null) {
            fullNameText.setText(String.format("%s %s %s",
                    selectedEmployee.getLastName(),
                    selectedEmployee.getFirstName(),
                    selectedEmployee.getPatronymic() != null ? selectedEmployee.getPatronymic() : ""));
            departmentText.setText(selectedEmployee.getDepartment() != null ?
                    selectedEmployee.getDepartment().getName() : "Не указан");
            baseSalaryText.setText(selectedEmployee.getDepartment() != null ?
                    selectedEmployee.getDepartment().getBaseSalary().toString() + " руб." : "Не указан");
            educationTypeText.setText(selectedEmployee.getEducationType() != null ?
                    selectedEmployee.getEducationType().getName() : "Не указан");
            coefficientText.setText(selectedEmployee.getEducationType() != null ?
                    selectedEmployee.getEducationType().getCoefficient().toString() : "Не указан");
            paymentDateText.setText(selectedSalary.getPaymentDate().toString());
            amountText.setText(selectedSalary.getAmount().toString() + " руб.");
            awardAmountText.setText(selectedSalary.getAwardAmount().toString() + " руб.");
            awardDescriptionText.setText(selectedSalary.getAwardDescription() != null ?
                    selectedSalary.getAwardDescription() : "");
            taxText.setText(selectedSalary.getTax().toString() + "%");
            sickDaysText.setText(calculateSickDays(selectedSalary.getSickLeaveStart(), selectedSalary.getSickLeaveEnd()) + " дн.");
            calculationText.setText(getSalaryCalculation());
        } else {
            clearPayslipData();
        }
    }

    private void clearPayslipData() {
        fullNameText.setText("");
        departmentText.setText("");
        baseSalaryText.setText("");
        educationTypeText.setText("");
        coefficientText.setText("");
        paymentDateText.setText("");
        amountText.setText("");
        awardAmountText.setText("");
        awardDescriptionText.setText("");
        taxText.setText("");
        sickDaysText.setText("");
        calculationText.setText("");
        selectedSalary = null;
    }

    private int calculateWorkingDaysInMonth(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        int workingDays = 0;
        for (LocalDate day = firstDay; !day.isAfter(lastDay); day = day.plusDays(1)) {
            if (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
        }

        return workingDays;
    }

    private int calculateSickDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return 0;
        }
        int sickDays = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY && current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                sickDays++;
            }
            current = current.plusDays(1);
        }
        return sickDays;
    }

    private String getSalaryCalculation() {
        if (selectedSalary == null || selectedEmployee == null || selectedEmployee.getDepartment() == null || selectedEmployee.getEducationType() == null) {
            return "Недостаточно данных для расчёта.";
        }

        BigDecimal baseSalary = selectedEmployee.getDepartment().getBaseSalary();
        BigDecimal coefficient = selectedEmployee.getEducationType().getCoefficient();
        BigDecimal award = selectedSalary.getAwardAmount();
        BigDecimal taxPercent = selectedSalary.getTax();
        int sickDays = calculateSickDays(selectedSalary.getSickLeaveStart(), selectedSalary.getSickLeaveEnd());
        BigDecimal serverAmount = selectedSalary.getAmount();

        // Расчёт: (База × Коэф.) - Больничные - Налог + Премия
        BigDecimal basicSalary = baseSalary.multiply(coefficient); // Без experienceMultiplier
        int workingDaysInMonth = calculateWorkingDaysInMonth(selectedSalary.getPaymentDate());
        BigDecimal salaryAdjustment = BigDecimal.ZERO;
        String sickLeaveNote = "";

        if (sickDays >= 10) {
            sickLeaveNote = String.format(" - Больничные (%d дн., оплачиваются): 0.00 руб.", sickDays);
        } else if (sickDays > 0) {
            BigDecimal dailySalary = basicSalary.divide(new BigDecimal(workingDaysInMonth), 2, RoundingMode.HALF_UP);
            salaryAdjustment = dailySalary.multiply(new BigDecimal(sickDays));
            sickLeaveNote = String.format(" - Больничные (%d дн., %d в месяц): -%.2f руб.", sickDays, workingDaysInMonth, salaryAdjustment);
        }

        BigDecimal taxes = basicSalary.multiply(taxPercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        BigDecimal calculatedFinal = basicSalary.subtract(salaryAdjustment).subtract(taxes).add(award);

        // Проверка расхождения с сервером
        String discrepancyNote = "";
        if (calculatedFinal.compareTo(serverAmount) != 0) {
            discrepancyNote = String.format(" (Серверная сумма: %.2f руб.)", serverAmount);
        }

        System.out.printf("Расчёт: База=%.2f, Коэф=%.2f, Больничные=%d, Рабочих дней=%d, Налог=%.2f, Премия=%.2f, Рассчитано=%.2f, Сервер=%.2f%n",
                baseSalary, coefficient, sickDays, workingDaysInMonth, taxes, award, calculatedFinal, serverAmount);

        return String.format("База: %.2f руб. × Коэф.: %.2f = %.2f руб.%s - Налог (%.2f%%): %.2f руб. + Премия: %.2f руб. = Итого: %.2f руб.%s",
                baseSalary, coefficient, basicSalary, sickLeaveNote, taxPercent, taxes, award, serverAmount, discrepancyNote);
    }

    private float showWrappedText(PDPageContentStream contentStream, String text, float x, float y, float maxWidth, PDType0Font font, float fontSize) throws IOException {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float currentWidth = 0;

        for (String word : words) {
            float wordWidth = font.getStringWidth(word + " ") / 1000 * fontSize;
            if (currentWidth + wordWidth <= maxWidth) {
                line.append(word).append(" ");
                currentWidth += wordWidth;
            } else {
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(line.toString().trim());
                contentStream.endText();
                y -= 15;
                line = new StringBuilder(word + " ");
                currentWidth = wordWidth;
            }
        }
        if (!line.toString().trim().isEmpty()) {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(line.toString().trim());
            contentStream.endText();
            y -= 15;
        }
        return y;
    }

    @FXML
    private void saveToPDF() {
        if (selectedSalary == null || selectedEmployee == null) {
            error("Ошибка", "Выберите сотрудника и дату выплаты для формирования расчётного листа.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить расчётный лист в PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF файлы", "*.pdf")
        );
        fileChooser.setInitialFileName("расчетный_лист_" + selectedEmployee.getFirstName() + "_" + selectedEmployee.getLastName() + "_" + selectedSalary.getPaymentDate().toString() + ".pdf");

        Stage stage = (Stage) savePdfButton.getScene().getWindow();
        if (stage == null) {
            error("Ошибка", "Не удалось определить окно для диалога сохранения.");
            return;
        }
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                InputStream fontStream = getClass().getResourceAsStream("/fonts/LiberationSans-Regular.ttf");
                if (fontStream == null) {
                    error("Ошибка", "Шрифт LiberationSans-Regular.ttf не найден в ресурсах.");
                    return;
                }
                PDType0Font font = PDType0Font.load(document, fontStream);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.beginText();
                    contentStream.setFont(font, 14);
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.showText("Расчётный лист");
                    contentStream.endText();

                    String[][] payslipData = {
                            {"ФИО:", String.format("%s %s %s",
                                    selectedEmployee.getLastName(),
                                    selectedEmployee.getFirstName(),
                                    selectedEmployee.getPatronymic() != null ? selectedEmployee.getPatronymic() : "")},
                            {"Отдел:", selectedEmployee.getDepartment() != null ?
                                    selectedEmployee.getDepartment().getName() : "Не указан"},
                            {"Базовая зарплата отдела:", selectedEmployee.getDepartment() != null ?
                                    selectedEmployee.getDepartment().getBaseSalary().toString() + " руб." : "Не указан"},
                            {"Тип образования:", selectedEmployee.getEducationType() != null ?
                                    selectedEmployee.getEducationType().getName() : "Не указан"},
                            {"Коэффициент образования:", selectedEmployee.getEducationType() != null ?
                                    selectedEmployee.getEducationType().getCoefficient().toString() : "Не указан"},
                            {"Дата выплаты:", selectedSalary.getPaymentDate().toString()},
                            {"Сумма зарплаты:", selectedSalary.getAmount().toString() + " руб."},
                            {"Премия:", selectedSalary.getAwardAmount().toString() + " руб."},
                            {"Описание премии:", selectedSalary.getAwardDescription() != null ?
                                    selectedSalary.getAwardDescription() : "Отсутствует"},
                            {"Налог (%):", selectedSalary.getTax().toString() + "%"},
                            {"Больничные дни:", calculateSickDays(selectedSalary.getSickLeaveStart(), selectedSalary.getSickLeaveEnd()) + " дн."},
                            {"Расчёт зарплаты:", getSalaryCalculation()}
                    };

                    float y = 690;
                    float labelX = 50;
                    float valueX = 200;
                    float maxWidth = 350;

                    for (String[] entry : payslipData) {
                        contentStream.beginText();
                        contentStream.setFont(font, 10);
                        contentStream.newLineAtOffset(labelX, y);
                        contentStream.showText(entry[0]);
                        contentStream.endText();

                        if (entry[0].equals("Расчёт зарплаты:")) {
                            y = showWrappedText(contentStream, entry[1], valueX, y, maxWidth, font, 10);
                        } else {
                            contentStream.beginText();
                            contentStream.setFont(font, 10);
                            contentStream.newLineAtOffset(valueX, y);
                            contentStream.showText(entry[1]);
                            contentStream.endText();
                            y -= 15;
                        }
                    }
                }

                document.save(file);
                info("Успех", "Расчётный лист успешно сохранён в: " + file.getAbsolutePath());
                System.out.println("PDF успешно сохранён в: " + file.getAbsolutePath());
            } catch (IOException ex) {
                error("Ошибка", "Не удалось сохранить PDF: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            System.out.println("Сохранение PDF отменено пользователем.");
        }
    }
}