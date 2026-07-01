package com.mitocode.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "payment_schedules")
public class PaymentScheduleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer simulationId;
    private Integer period;
    private LocalDate date;
    private BigDecimal initialBalance;
    private BigDecimal payment;
    private BigDecimal balloonPayment = BigDecimal.ZERO;
    private BigDecimal interest;
    private BigDecimal amortization;
    private BigDecimal insurance;
    private BigDecimal commission;
    private BigDecimal totalPayment;
    private BigDecimal finalBalance;
    @Column(name="grace_type") private String graceType;
}
