package com.mitocode.dto;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class PaymentRow {
    private Integer period;
    private LocalDate date;
    private BigDecimal initialBalance;
    private BigDecimal payment;
    private BigDecimal balloonPayment;
    private BigDecimal interest;
    private BigDecimal amortization;
    private BigDecimal insurance;
    private BigDecimal commission;
    private BigDecimal totalPayment;
    private BigDecimal finalBalance;
    private String graceType;
}
