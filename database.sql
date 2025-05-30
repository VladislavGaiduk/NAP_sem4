CREATE ROLE salary_user WITH LOGIN PASSWORD 'password123';

-- Создание таблицы departments
CREATE TABLE departments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    base_salary DECIMAL(10,2) NOT NULL
);

-- Создание таблицы education_types
CREATE TABLE education_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    coefficient DECIMAL(5,2) NOT NULL
);

-- Создание таблицы users
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    patronymic VARCHAR(255)
);

-- Создание таблицы employees
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    patronymic VARCHAR(255),
    login VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    hire_date DATE NOT NULL,
    department_id INTEGER REFERENCES departments(id),
    education_type_id INTEGER REFERENCES education_types(id)
);

-- Создание таблицы salaries
CREATE TABLE salaries (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    sick_leave_start DATE,
    sick_leave_end DATE,
    award_amount DECIMAL(10,2),
    award_description TEXT,
    tax DECIMAL(10,2)
);

GRANT ALL PRIVILEGES ON DATABASE sms_db TO salary_user;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO salary_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO salary_user;

-- Вставка данных в таблицу departments
INSERT INTO departments (name, base_salary) VALUES
    ('Инженерный', 1800.00),
    ('Бухгалтерия', 1500.00),
    ('ИТ', 2000.00),
    ('Маркетинг', 1700.00),
    ('Логистика', 1600.00),
    ('HR', 1400.00),
    ('Производство', 2100.00),
    ('Юридический', 2200.00);

-- Вставка данных в таблицу education_types
INSERT INTO education_types (name, coefficient) VALUES
    ('Высшее', 1.20),
    ('Среднее специальное', 1.10),
    ('Без образования', 1.00),
    ('Кандидат наук', 1.50);

-- Вставка данных в таблицу users
INSERT INTO users (username, password_hash, first_name, last_name, patronymic) VALUES
    ('pavlov', 'fc9c6940a9162937a02380419b64fd7d38c9de6a6502baa49d16571408ec01a2', 'Эдуард', 'Петров', 'Павлович');

-- Вставка данных в таблицу employees
-- Пароли: password123 (SHA-256: ef92b778ba...)
INSERT INTO employees (first_name, last_name, patronymic, login, password_hash, hire_date, department_id, education_type_id) VALUES
    ('Владислав', 'Гайдук', 'Дмитриевич', 'vgaiduk', '2d6ea97265bf3aef848f3dff1e3548ff94a2885974d768d1b6bb37ea419a6a14', '2023-06-01', 2, 1), -- Инженерный, Высшее
    ('Екатерина', 'Смирнова', 'Алексеевна', 'ekaterina', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2020-01-15', 2, 2), -- Бухгалтерия, Среднее специальное
    ('Алексей', 'Иванов', 'Петрович', 'aivanov', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2018-03-10', 3, 1), -- ИТ, Высшее
    ('Мария', 'Петрова', NULL, 'mpetrova', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2024-02-01', 4, 3), -- Маркетинг, Без образования
    ('Сергей', 'Ковалёв', 'Викторович', 'skovalev', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2021-09-01', 5, 1), -- Логистика, Высшее
    ('Ольга', 'Морозова', 'Сергеевна', 'omorozova', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2022-11-15', 5, 2), -- Логистика, Среднее специальное
    ('Дмитрий', 'Соколов', 'Андреевич', 'dsokolov', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2019-05-20', 6, 3), -- Кадры, Без образования
    ('Анна', 'Волкова', 'Игоревна', 'avolkova', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2023-03-10', 6, 1), -- Кадры, Высшее
    ('Николай', 'Зайцев', 'Михайлович', 'nzaicev', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2020-07-01', 7, 2), -- Продажи, Среднее специальное
    ('Елена', 'Козлова', 'Владимировна', 'ekozlova', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2024-01-05', 7, 1), -- Продажи, Высшее
    ('Игорь', 'Медведев', 'Александрович', 'imedvedev', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2021-04-12', 8, 4), -- Производство, Кандидат наук
    ('Татьяна', 'Романова', 'Николаевна', 'tromanova', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2022-08-25', 8, 2), -- Производство, Среднее специальное
    ('Виктор', 'Орлов', 'Евгеньевич', 'vorlov', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2019-12-01', 9, 1), -- Юридический, Высшее
    ('Светлана', 'Громова', 'Олеговна', 'sgromova', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '2023-10-15', 9, 4); -- Юридический, Кандидат наук

-- Вставка данных в таблицу salaries
INSERT INTO salaries (employee_id, amount, payment_date, sick_leave_start, sick_leave_end, award_amount, award_description, tax) VALUES
    (1, 2281.02, '2025-05-15', '2025-05-10', '2025-05-10', 500.00, 'за красивые глазки', 13.00), -- Лапич, 1 больничный день
    (2, 1500.00, '2025-05-15', NULL, NULL, 200.00, 'за переработку', 13.00), -- Смирнова, без больничных
    (3, 2300.00, '2025-05-15', '2025-05-01', '2025-05-12', 0.00, NULL, 13.00), -- Иванов, 10+ больничных (оплачиваются)
    (4, 1600.00, '2025-04-15', '2025-04-05', '2025-04-07', 100.00, 'за креатив', 13.00); -- Петрова, 3 больничных дня