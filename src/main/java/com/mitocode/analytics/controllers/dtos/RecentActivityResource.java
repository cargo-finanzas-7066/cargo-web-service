package com.mitocode.analytics.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RecentActivityResource {
    private String client;
    private String vehicle;
    private String financialInstitution;
    private BigDecimal financedAmount;
    private BigDecimal tea;
    private BigDecimal tcea;
    private BigDecimal monthlyPayment;
}
