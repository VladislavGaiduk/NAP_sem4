package com.example.server.utils;

import com.example.server.models.Employee;
import com.example.server.models.Salary;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class DatabaseHandler {
    public DatabaseHandler() {
        // Hibernate автоматически инициализируется через HibernateUtil
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хеширования пароля", e);
        }
    }

    // Методы для сотрудников и зарплат (оставлены без изменений)
    public List<Employee> getAllEmployees() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Employee> query = session.createQuery("FROM Employee", Employee.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("Ошибка при получении сотрудников: " + e.getMessage());
            return null;
        }
    }

    public Employee getEmployee(String identifier) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Employee> query = session.createQuery("FROM Employee WHERE id = :id", Employee.class);
            query.setParameter("id", Long.parseLong(identifier));
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("Ошибка при получении сотрудника: " + e.getMessage());
            return null;
        }
    }

    public List<Salary> getAllSalaries() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Salary> query = session.createQuery("FROM Salary", Salary.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("Ошибка при получении зарплат: " + e.getMessage());
            return null;
        }
    }

    public void close() {
        HibernateUtil.shutdown();
    }


}