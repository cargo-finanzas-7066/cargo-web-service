package com.mitocode.vehicles.controllers.dtos;

import com.mitocode.shared.paging.SortableField;

public enum VehicleSortField implements SortableField {
    ID("id"),
    BRAND("brand"),
    MODEL("model"),
    YEAR("year"),
    CATEGORY("category"),
    PRICE("price"),
    DEALER("dealer"),
    STATUS("status"),
    CREATED_AT("createdAt");

    private final String property;

    VehicleSortField(String property) {
        this.property = property;
    }

    @Override
    public String getProperty() {
        return property;
    }
}
