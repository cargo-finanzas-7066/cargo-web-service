package com.mitocode.customers.controllers.dtos;

import com.mitocode.shared.paging.SortableField;

public enum CustomerSortField implements SortableField {
    ID("id"),
    NAMES("names"),
    SURNAMES("surnames"),
    EMAIL("email"),
    PHONE("phone"),
    STATUS("status"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String property;

    CustomerSortField(String property) {
        this.property = property;
    }

    @Override
    public String getProperty() {
        return property;
    }
}
