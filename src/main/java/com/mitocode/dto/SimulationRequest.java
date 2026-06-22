package com.mitocode.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SimulationRequest {
    private Integer id;
    private Integer clientId;
    private Integer vehicleId;
    private Integer entityId;
    private String currency = "PEN";
    private Double vehiclePrice;
    private Double downPayment;
    private Double downPaymentPercent = 20.0;
    private Integer term;
    private Double tea;
    private Integer paymentDay = 5;
    private LocalDate disbursementDate;
    private String graceType = "none";
    private Integer graceMonths = 0;
    private Boolean balloonEnabled = false;
    private Double balloonAmount = 0.0;
    private Double insuranceDisbursement = 0.05;
    private Double insuranceVehicle = 3.5;
    private Double monthlyFee = 0.0;
    private Double adminCost = 0.0;
    private Double notaryCost = 0.0;
    private Double otherCharges = 0.0;
    private String status = "Borrador";
}
