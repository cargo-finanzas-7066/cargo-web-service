package com.mitocode.financialinstitutions.controllers.dtos;

import com.mitocode.shared.paging.SortableField;

public enum FinancialProductSortField implements SortableField {
    ID("id"),
    PRODUCT_NAME("productName"),
    TEA_PERCENT("teaPercent"),
    VALID_FROM("validFrom"),
    VALID_UNTIL("validUntil");

    private final String property;

    FinancialProductSortField(String property) {
        this.property = property;
    }

    @Override
    public String getProperty() {
        return property;
    }
}
