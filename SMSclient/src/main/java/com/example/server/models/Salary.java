package com.example.server.models;

import com.google.gson.annotations.Expose;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "salaries")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Salary implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Expose
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @Expose
    private Employee employee;

    @Column(name = "amount", nullable = false)
    @Expose
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    @Expose
    private LocalDate paymentDate;

    @Column(name = "sick_leave_start")
    @Expose
    private LocalDate sickLeaveStart;

    @Column(name = "sick_leave_end")
    @Expose
    private LocalDate sickLeaveEnd;

    @Column(name = "award_amount")
    @Expose
    private BigDecimal awardAmount;

    @Column(name = "award_description")
    @Expose
    private String awardDescription;

    @Column(name = "tax")
    @Expose
    private BigDecimal tax;
}