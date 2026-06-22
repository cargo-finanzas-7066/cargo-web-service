package com.mitocode.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PaymentRow {
    private Integer period;
    private LocalDate date;
    private Double initialBalance;
    private Double payment;
    private Double balloonPayment;
    private Double interest;
    private Double amortization;
    private Double insurance;
    private Double commission;
    private Double totalPayment;
    private Double finalBalance;
}
