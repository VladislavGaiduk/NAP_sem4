package com.example.server.dao;

import com.example.server.models.Employee;
import com.example.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

import static com.example.server.utils.DatabaseHandler.hashPassword;

public class EmployeeDAO {

    public void save(Employee employee) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            employee.setPasswordHash(hashPassword(employee.getPasswordHash()));
            session.save(employee);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void update(Employee employee) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            if (!employee.getPasswordHash().isEmpty()) {
                employee.setPasswordHash(hashPassword(employee.getPasswordHash()));
            }
            session.update(employee);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Employee employee) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.delete(employee);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public Employee findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Employee.class, id);
        }
    }

    public Employee findByLogin(String login) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Employee> employeeQuery = session.createQuery(
                    "FROM Employee WHERE login = :login", Employee.class);
            employeeQuery.setParameter("login", login);
            Employee employee = employeeQuery.uniqueResult();
            return employee;
        }
    }

    public List<Employee> searchEmployees(String firstName, String lastName, String departmentName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Employee e WHERE 1=1");
            if (firstName != null && !firstName.isEmpty()) {
                hql.append(" AND e.firstName LIKE :firstName");
            }
            if (lastName != null && !lastName.isEmpty()) {
                hql.append(" AND e.lastName LIKE :lastName");
            }
            if (departmentName != null && !departmentName.isEmpty()) {
                hql.append(" AND e.department.name LIKE :departmentName");
            }
            Query<Employee> query = session.createQuery(hql.toString(), Employee.class);
            if (firstName != null && !firstName.isEmpty()) {
                query.setParameter("firstName", "%" + firstName + "%");
            }
            if (lastName != null && !lastName.isEmpty()) {
                query.setParameter("lastName", "%" + lastName + "%");
            }
            if (departmentName != null && !departmentName.isEmpty()) {
                query.setParameter("departmentName", "%" + departmentName + "%");
            }
            return query.list();
        }
    }

    public List<Employee> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Employee> query = session.createQuery("FROM Employee", Employee.class);
            return query.list();
        }
    }
}