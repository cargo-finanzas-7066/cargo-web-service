package com.mitocode.vehicles.controllers.dtos;

import lombok.Data;

@Data
public class VehicleResource {
    private Integer id;
    private String brand;
    private String model;
    private Integer year;
    private String category;
    private Double price;
    private String currency;
    private String dealer;
    private String description;
    private String imageUrl;
    private String status;
}
