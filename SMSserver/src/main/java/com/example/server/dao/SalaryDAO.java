package com.example.server.dao;

import com.example.server.models.Salary;
import com.example.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

public class SalaryDAO {

    public void save(Salary salary) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(salary);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void update(Salary salary) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(salary);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Salary salary) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.delete(salary);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public boolean handleCheckSalaryExists(String data) {
        String[] parts = data.split(",");
        Integer employeeId = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);
        int month = Integer.parseInt(parts[2]);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Salary> query = session.createQuery(
                    "FROM Salary WHERE employee.id = :employeeId AND YEAR(paymentDate) = :year AND MONTH(paymentDate) = :month", Salary.class);
            query.setParameter("employeeId", employeeId);
            query.setParameter("year", year);
            query.setParameter("month", month);
            Salary salary = query.uniqueResult();
            return salary != null;
        }
    }

    public boolean handleCheckOtherSalaryExists(String data) {
        String[] parts = data.split(",");
        Integer excludeSalaryId = Integer.parseInt(parts[0]);
        Integer employeeId = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        int month = Integer.parseInt(parts[3]);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Salary> query = session.createQuery(
                    "FROM Salary WHERE employee.id = :employeeId AND YEAR(paymentDate) = :year AND MONTH(paymentDate) = :month AND id != :excludeSalaryId", Salary.class);
            query.setParameter("employeeId", employeeId);
            query.setParameter("year", year);
            query.setParameter("month", month);
            query.setParameter("excludeSalaryId", excludeSalaryId);
            Salary salary = query.uniqueResult();
            return salary != null;
        }
    }

    public Salary findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Salary.class, id);
        }
    }

    public List<Salary> filterSalaries(Integer employeeId, LocalDate paymentDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Salary WHERE 1=1");
            if (employeeId != null) {
                hql.append(" AND employee.id = :employeeId");
            }
            if (paymentDate != null) {
                hql.append(" AND paymentDate = :paymentDate");
            }
            Query<Salary> query = session.createQuery(hql.toString(), Salary.class);
            if (employeeId != null) {
                query.setParameter("employeeId", employeeId);
            }
            if (paymentDate != null) {
                query.setParameter("paymentDate", paymentDate);
            }
            return query.list();
        }
    }

    public List<Salary> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Salary> query = session.createQuery("FROM Salary", Salary.class);
            return query.list();
        }
    }
}