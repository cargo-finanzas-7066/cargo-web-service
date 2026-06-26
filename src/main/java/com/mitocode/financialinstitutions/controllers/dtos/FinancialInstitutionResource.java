package com.mitocode.financialinstitutions.controllers.dtos;

import lombok.Data;

@Data
public class FinancialInstitutionResource {
    private Integer id;
    private String code;
    private Integer displayOrder;
    private String name;
    private String shortName;
    private String type;
    private String logoText;
    private String currency;
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
    private Integer minTerm;
    private Integer maxTerm;
    private Double minDownPayment;
    private Double maxFinancing;
    private Double insuranceDisbursement;
    private Double insuranceVehicle;
    private Double monthlyFee;
    private Double adminCost;
    private String ratesJson;
    private String insurancesJson;
    private String chargesJson;
    private String sourceName;
    private String sourceDate;
    private String verificationStatus;
    private Boolean canUseInSimulation;
    private String status;
}
