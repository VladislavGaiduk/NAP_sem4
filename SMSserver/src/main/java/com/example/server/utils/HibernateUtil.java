package com.example.server.utils;

import com.example.server.models.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {
    private static final SessionFactory sessionFactory;

    static {
        try {
            // Создаём конфигурацию
            Configuration configuration = new Configuration();

            // Добавляем сущности
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Department.class);
            configuration.addAnnotatedClass(Employee.class);
            configuration.addAnnotatedClass(Salary.class);
            configuration.addAnnotatedClass(EducationType.class);

            // Загружаем настройки из hibernate.properties
            //  configuration.configure("hibernate.properties");

            // Создаём ServiceRegistry и SessionFactory
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            System.err.println("Ошибка инициализации Hibernate: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}