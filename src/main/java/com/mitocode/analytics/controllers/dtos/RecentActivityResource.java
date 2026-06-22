package com.mitocode.analytics.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecentActivityResource {
    private String client;
    private String vehicle;
    private String financialInstitution;
    private Double financedAmount;
    private Double tea;
    private Double tcea;
    private Double monthlyPayment;
}
