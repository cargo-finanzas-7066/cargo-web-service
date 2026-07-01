package com.mitocode.customers.controllers.dtos;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class CustomerResource {
    private Integer id;
    @NotBlank @Size(max=20) private String docType;
    @NotBlank @Size(max=30) private String docNumber;
    @NotBlank @Size(max=120) private String names;
    @NotBlank @Size(max=120) private String surnames;
    @Email @Size(max=254) private String email;
    private String phone;
    private String address;
    @PositiveOrZero private BigDecimal monthlyIncome;
    private String occupation;
    private String status;
}
