package com.mitocode.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "financial_entities")
public class FinancialEntityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String type;
    private String product;
    private Double tea;
    private Integer minTerm = 12;
    private Integer maxTerm = 60;
    private Double minDownPayment = 20.0;
    private Double insuranceDisbursement = 0.05;
    private Double insuranceVehicle = 3.5;
    private Double monthlyFee = 0.0;
    private Double adminCost = 0.0;
    @Column(columnDefinition = "TEXT")
    private String penalties;
    private String status = "Activo";
}
