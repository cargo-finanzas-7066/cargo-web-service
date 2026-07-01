package com.mitocode.dto;

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
    @NotNull private LocalDate firstPaymentDate;
    @NotNull @Min(1) @Max(28) private Integer paymentDay;
    @NotNull private GraceType graceType = GraceType.NONE;
    @NotNull @Min(0) private Integer graceMonths = 0;
    @NotNull @DecimalMin("0.0") @DecimalMax("99.9999") private BigDecimal balloonPercent = BigDecimal.ZERO;
}
