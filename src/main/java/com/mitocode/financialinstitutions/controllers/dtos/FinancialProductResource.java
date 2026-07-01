package com.mitocode.financialinstitutions.controllers.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FinancialProductResource {
    private Integer id;
    @NotNull private Integer financialInstitutionId;
    private String institutionCode;
    private String institutionName;
    @NotBlank @Size(max=160) private String productName;
    private Integer version;
    @NotBlank @Pattern(regexp="[A-Z]{3}") private String currency;
    @NotNull @DecimalMin("0.0") private BigDecimal teaPercent;
    @NotNull @Min(1) private Integer minTermMonths;
    @NotNull @Min(1) private Integer maxTermMonths;
    @NotNull @DecimalMin("0.0") @DecimalMax("100.0") private BigDecimal minDownPaymentPercent;
    @NotNull @DecimalMin("0.0") @DecimalMax("100.0") private BigDecimal maxDownPaymentPercent;
    @NotNull private Boolean balloonAllowed;
    @NotNull @DecimalMin("0.0") @DecimalMax("99.9999") private BigDecimal maxBalloonPercent;
    @NotNull @DecimalMin("0.0") private BigDecimal creditLifeInsuranceMonthlyPercent;
    @NotNull @DecimalMin("0.0") private BigDecimal vehicleInsuranceAnnualPercent;
    @NotNull @PositiveOrZero private BigDecimal monthlyFee;
    @NotNull @PositiveOrZero private BigDecimal adminCost;
    @NotNull @PositiveOrZero private BigDecimal notaryCost;
    @NotNull @PositiveOrZero private BigDecimal otherUpfrontCost;
    @NotNull private Boolean capitalizeInterestTotalGrace;
    @NotNull private Boolean capitalizeInsuranceTotalGrace;
    @NotNull private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean active;
}
