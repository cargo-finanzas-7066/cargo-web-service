package com.mitocode.dto;

import lombok.Data;
import java.util.List;

@Data
public class SimulationResult {
    private Double monthlyPayment;
    private Double balloonAmount;
    private Double tea;
    private Double tem;
    private Double van;
    private Double tir;
    private Double tcea;
    private Double financedAmount;
    private Double totalInterest;
    private Double totalInsurance;
    private Double totalCommissions;
    private Double totalCreditCost;
    private Double totalPayment;
    private List<PaymentRow> schedule;
}
