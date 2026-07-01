package com.mitocode.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Map;

@Data
public class SimulationResource {
    private Integer id;
    private String code;
    private Integer clientId;
    private Integer vehicleId;
    private Integer financialProductId;
    private String currency;
    private BigDecimal vehiclePrice;
    private BigDecimal downPaymentPercent;
    private BigDecimal financedAmount;
    private Integer termMonths;
    private LocalDate firstPaymentDate;
    private Integer paymentDay;
    private String graceType;
    private Integer graceMonths;
    private BigDecimal balloonPercent;
    private BigDecimal monthlyPayment;
    private BigDecimal teaPercent;
    private BigDecimal temPercent;
    private BigDecimal tirPercent;
    private BigDecimal tceaPercent;
    private BigDecimal van;
    private BigDecimal totalInterest;
    private BigDecimal totalInsurance;
    private BigDecimal totalFees;
    private BigDecimal totalPayment;
    private Map<String,Object> productSnapshot;
    private OffsetDateTime createdAt;
    private List<PaymentRow> schedule;
}
