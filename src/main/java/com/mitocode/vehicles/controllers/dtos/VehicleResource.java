package com.mitocode.vehicles.controllers.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class VehicleResource {
    private Integer id;
    @NotBlank @Size(max=80) private String code;
    @NotBlank @Size(max=100) private String brand;
    @NotBlank @Size(max=120) private String model;
    @NotNull @Min(1900) @Max(2100) private Integer year;
    private String category;
    @NotNull @PositiveOrZero private BigDecimal price;
    @Pattern(regexp="[A-Z]{3}") private String currency;
    private String dealer;
    private String description;
    private String imageUrl;
    private String status;
}
