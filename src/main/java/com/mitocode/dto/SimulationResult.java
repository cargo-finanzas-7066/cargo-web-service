package com.mitocode.dto;

import lombok.Data;
import java.util.List;

@Data
public class SimulationResult {
    private Double monthlyPayment;
    private Double van;
    private Double tir;
    private Double tcea;
    private Double financedAmount;
    private Double totalPayment;
    private List<PaymentRow> schedule;
}
