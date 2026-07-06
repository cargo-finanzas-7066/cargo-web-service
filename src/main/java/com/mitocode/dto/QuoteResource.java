package com.mitocode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class QuoteResource {
    private Integer financialProductId;
    private String institutionCode;
    private String institutionName;
    private String productName;
    private Integer productVersion;
    private SimulationResult result;
}
