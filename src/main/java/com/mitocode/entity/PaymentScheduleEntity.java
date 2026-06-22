package com.mitocode.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

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
    private Double initialBalance;
    private Double payment;
    private Double interest;
    private Double amortization;
    private Double insurance;
    private Double commission;
    private Double finalBalance;
}
