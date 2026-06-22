package com.mitocode.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "simulations")
public class SimulationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String code;
    private Integer clientId;
    private Integer vehicleId;
    private Integer entityId;
    private String currency = "PEN";
    private Double vehiclePrice;
    private Double downPayment;
    private Double financedAmount;
    private Integer term;
    private Double tea;
    private Integer paymentDay = 5;
    private LocalDate disbursementDate;
    private String graceType = "none";
    private Integer graceMonths = 0;
    private Double insuranceDisbursement = 0.05;
    private Double insuranceVehicle = 3.5;
    private Double monthlyFee = 0.0;
    private Double adminCost = 0.0;
    private Double notaryCost = 0.0;
    private Double otherCharges = 0.0;
    private String status = "Borrador";
    private LocalDate createdAt;
}
