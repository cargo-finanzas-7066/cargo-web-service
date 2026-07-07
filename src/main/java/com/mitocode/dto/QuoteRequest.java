package com.mitocode.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class QuoteRequest {
    @NotNull private Integer clientId;
    @NotNull private Integer vehicleId;
    @NotEmpty private List<Integer> financialProductIds;
    @Positive private BigDecimal vehiclePrice;
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
