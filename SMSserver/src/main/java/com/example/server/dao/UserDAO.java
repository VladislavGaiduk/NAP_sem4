package com.example.server.dao;

import com.example.server.models.Employee;
import com.example.server.models.User;
import com.example.server.utils.GsonHolder;
import com.example.server.utils.HibernateUtil;
import com.example.server.utils.LoginResult;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.JSONObject;

import java.util.List;

import static com.example.server.utils.DatabaseHandler.hashPassword;

public class UserDAO {

    public void save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            user.setPasswordHash(hashPassword(user.getPasswordHash()));
            transaction = session.beginTransaction();
            session.update(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.delete(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public User findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        }
    }

    public User findByName(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> userQuery = session.createQuery(
                    "FROM User WHERE username = :username", User.class);
            userQuery.setParameter("username", username);

            User user = userQuery.uniqueResult();
            return user;
        }
    }

    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.list();
        }
    }

    public LoginResult loginUser(String username, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Сначала проверяем в таблице users
            String password1 = hashPassword(password);
            Query<User> userQuery = session.createQuery(
                    "FROM User WHERE username = :username AND passwordHash = :password", User.class);
            userQuery.setParameter("username", username);
            userQuery.setParameter("password", password1);
            User user = userQuery.uniqueResult();

            if (user != null) {
                return new LoginResult(true, "users", null);
            }

            // Если в users не нашли, проверяем в employees
            Query<Employee> employeeQuery = session.createQuery(
                    "FROM Employee WHERE login = :username AND passwordHash = :password", Employee.class);
            employeeQuery.setParameter("username", username);
            employeeQuery.setParameter("password", password1);
            Employee employee = employeeQuery.uniqueResult();
            String gson = GsonHolder.getGson().toJson(employee);
            if (employee != null) {
                return new LoginResult(true, "employees", gson);
            }

            return new LoginResult(false, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResult(false, null, null);
        }
    }

    public JSONObject getUserData(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);
            User user = query.uniqueResult();

            if (user != null) {
                JSONObject userData = new JSONObject();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("first_name", user.getFirstName());
                userData.put("last_name", user.getLastName());
                userData.put("patronymic", user.getPatronymic());
                return userData;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject getEmployeeData(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Employee> query = session.createQuery(
                    "FROM Employee WHERE login = :username", Employee.class);
            query.setParameter("username", username);
            Employee employee = query.uniqueResult();

            if (employee != null) {
                JSONObject employeeData = new JSONObject();
                employeeData.put("id", employee.getId());
                employeeData.put("username", employee.getLogin());
                employeeData.put("first_name", employee.getFirstName());
                employeeData.put("last_name", employee.getLastName());
                employeeData.put("patronymic", employee.getPatronymic());
                return employeeData;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean registerUser(String username, String password, String firstName, String lastName, String patronymic) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(password); // Предполагаем, что пароль уже хеширован
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPatronymic(patronymic);


            session.save(user);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }
}