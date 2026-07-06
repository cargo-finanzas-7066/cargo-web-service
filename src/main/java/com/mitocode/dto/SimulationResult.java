package com.mitocode.dto;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

@Data
public class SimulationResult {
    private BigDecimal monthlyPayment;
    private BigDecimal balloonAmount;
    private BigDecimal tea;
    private BigDecimal tem;
    private BigDecimal cokTeaPercent;
    private BigDecimal cokTemPercent;
    private BigDecimal van;
    private BigDecimal tir;
    private BigDecimal tcea;
    private BigDecimal financedAmount;
    private BigDecimal totalInterest;
    private BigDecimal totalInsurance;
    private BigDecimal totalCommissions;
    private BigDecimal totalCreditCost;
    private BigDecimal totalPayment;
    private List<PaymentRow> schedule;
}
