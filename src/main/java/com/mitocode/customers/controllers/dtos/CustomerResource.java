package com.mitocode.customers.controllers.dtos;

import lombok.Data;

@Data
public class CustomerResource {
    private Integer id;
    private String docType;
    private String docNumber;
    private String names;
    private String surnames;
    private String email;
    private String phone;
    private String address;
    private Double monthlyIncome;
    private String occupation;
    private String status;
}
