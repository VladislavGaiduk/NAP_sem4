package com.example.server.models;

import com.google.gson.annotations.Expose;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Expose
    private Integer id;

    @Column(name = "first_name", nullable = false)
    @Expose
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @Expose
    private String lastName;

    @Column(name = "patronymic")
    @Expose
    private String patronymic;

    @Column(name = "login", nullable = false, unique = true)
    @Expose
    private String login;

    @Column(name = "password_hash", nullable = false)
    @Expose
    private String passwordHash;

    @Column(name = "hire_date", nullable = false)
    @Expose
    private LocalDate hireDate;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @Expose
    private Department department;

    @ManyToOne
    @JoinColumn(name = "education_type_id")
    @Expose
    private EducationType educationType;
}