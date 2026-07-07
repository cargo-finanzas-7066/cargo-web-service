package com.mitocode.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SimulationRequest {
    @NotNull private Integer clientId;
    @NotNull private Integer vehicleId;
    @NotNull private Integer financialProductId;
    @Positive private BigDecimal vehiclePrice;
    @DecimalMin("0.0") private BigDecimal teaPercent;
    @NotNull @DecimalMin("0.0") @DecimalMax("100.0") private BigDecimal downPaymentPercent;
    @NotNull @Min(1) private Integer termMonths;
    @JsonAlias({"cok", "cokTea", "cokPercent", "discountRate", "discountRatePercent", "tasaDescuento", "tasaDescuentoPercent"})
    @NotNull @DecimalMin("0.0") private BigDecimal cokTeaPercent;
    @NotNull private LocalDate firstPaymentDate;
    @NotNull @Min(1) @Max(28) private Integer paymentDay;
    @NotNull private GraceType graceType = GraceType.NONE;
    @NotNull @Min(0) private Integer graceMonths = 0;
    @NotNull @DecimalMin("0.0") @DecimalMax("99.9999") private BigDecimal balloonPercent = BigDecimal.ZERO;
    @NotNull private Boolean creditLifeInsuranceEnabled = true;
    @DecimalMin("0.0") private BigDecimal creditLifeInsuranceMonthlyPercent;
    @NotNull private Boolean vehicleInsuranceEnabled = true;
    @DecimalMin("0.0") private BigDecimal vehicleInsuranceAnnualPercent;
}
