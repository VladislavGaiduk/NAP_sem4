package com.example.server.models;

import com.google.gson.annotations.Expose;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "education_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationType implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Expose
    private Integer id;

    @Column(name = "name", nullable = false)
    @Expose
    private String name;

    @Column(name = "coefficient", nullable = false)
    @Expose
    private BigDecimal coefficient;
}