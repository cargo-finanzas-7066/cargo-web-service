package com.mitocode.financialinstitutions.persistence.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Entity
@Table(name = "financial_products", uniqueConstraints = @UniqueConstraint(columnNames = {"financial_institution_id", "product_name", "version"}))
public class FinancialProductEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "financial_institution_id", nullable = false)
    private FinancialInstitutionEntity financialInstitution;
    @Column(name = "product_name", nullable = false) private String productName;
    @Column(nullable = false) private Integer version;
    @Column(nullable = false, length = 3) private String currency = "PEN";
    @Column(name = "tea_percent", nullable = false, precision = 12, scale = 7) private BigDecimal teaPercent;
    @Column(name = "min_term_months", nullable = false) private Integer minTermMonths;
    @Column(name = "max_term_months", nullable = false) private Integer maxTermMonths;
    @Column(name = "min_down_payment_percent", nullable = false, precision = 7, scale = 4) private BigDecimal minDownPaymentPercent;
    @Column(name = "max_down_payment_percent", nullable = false, precision = 7, scale = 4) private BigDecimal maxDownPaymentPercent = new BigDecimal("100");
    @Column(name = "balloon_allowed", nullable = false) private Boolean balloonAllowed = false;
    @Column(name = "max_balloon_percent", nullable = false, precision = 7, scale = 4) private BigDecimal maxBalloonPercent = BigDecimal.ZERO;
    @Column(name = "credit_life_insurance_monthly_percent", nullable = false, precision = 12, scale = 7) private BigDecimal creditLifeInsuranceMonthlyPercent = BigDecimal.ZERO;
    @Column(name = "vehicle_insurance_annual_percent", nullable = false, precision = 12, scale = 7) private BigDecimal vehicleInsuranceAnnualPercent = BigDecimal.ZERO;
    @Column(name = "monthly_fee", nullable = false, precision = 19, scale = 2) private BigDecimal monthlyFee = BigDecimal.ZERO;
    @Column(name = "admin_cost", nullable = false, precision = 19, scale = 2) private BigDecimal adminCost = BigDecimal.ZERO;
    @Column(name = "notary_cost", nullable = false, precision = 19, scale = 2) private BigDecimal notaryCost = BigDecimal.ZERO;
    @Column(name = "other_upfront_cost", nullable = false, precision = 19, scale = 2) private BigDecimal otherUpfrontCost = BigDecimal.ZERO;
    @Column(name = "capitalize_interest_total_grace", nullable = false) private Boolean capitalizeInterestTotalGrace = true;
    @Column(name = "capitalize_insurance_total_grace", nullable = false) private Boolean capitalizeInsuranceTotalGrace = true;
    @Column(name = "valid_from", nullable = false) private LocalDate validFrom;
    @Column(name = "valid_until") private LocalDate validUntil;
    @Column(nullable = false) private Boolean active = true;
}
