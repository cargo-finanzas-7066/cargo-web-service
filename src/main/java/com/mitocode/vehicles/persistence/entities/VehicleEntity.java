package com.mitocode.vehicles.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "vehicles")
public class VehicleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private String code;
    private String brand;
    private String model;
    private Integer year;
    private String category;
    private Double price;
    private String currency = "PEN";
    private String dealer;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String imageUrl;
    private String status = "Disponible";
}
