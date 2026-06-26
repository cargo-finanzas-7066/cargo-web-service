package com.mitocode.financialinstitutions.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "financial_entities")
public class FinancialInstitutionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private String code;
    private Integer displayOrder;
    private String name;
    private String shortName;
    private String type;
    private String logoText;
    private String currency = "PEN";
    private String creditType;
    private String product;
    private String teaPublishedLabel;
    private String minimumInitialLabel;
    private String maximumFinancingLabel;
    private String termLabel;
    private String graceLabel;
    private String insuranceSummaryLabel;
    private String chargesSummaryLabel;
    private Double tea;
    private Integer minTerm = 12;
    private Integer maxTerm = 60;
    private Double minDownPayment = 20.0;
    private Double maxFinancing;
    private Double insuranceDisbursement = 0.0;
    private Double insuranceVehicle = 0.0;
    private Double monthlyFee = 0.0;
    private Double adminCost = 0.0;
    @Column(columnDefinition = "TEXT")
    private String ratesJson;
    @Column(columnDefinition = "TEXT")
    private String insurancesJson;
    @Column(columnDefinition = "TEXT")
    private String chargesJson;
    private String sourceName;
    private String sourceDate;
    private String verificationStatus;
    private Boolean canUseInSimulation = true;
    private String status = "Activo";
}
