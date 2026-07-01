package com.mitocode.dto;

import com.mitocode.shared.paging.SortableField;

public enum SimulationSortField implements SortableField {
    ID("id"),
    CODE("code"),
    STATUS("status"),
    TERM("term"),
    MONTHLY_PAYMENT("monthlyPayment"),
    TCEA("tcea"),
    CREATED_AT("createdAt");

    private final String property;

    SimulationSortField(String property) {
        this.property = property;
    }

    @Override
    public String getProperty() {
        return property;
    }
}
