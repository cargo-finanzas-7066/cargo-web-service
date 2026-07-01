package com.mitocode.vehicles.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

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
    @Column(precision=19, scale=2) private BigDecimal price;
    private String currency = "PEN";
    private String dealer;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String imageUrl;
    private String status = "Disponible";
    @Column(nullable = false) private Boolean active = true;
    @Column(name = "created_at", insertable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false) private OffsetDateTime updatedAt;
}
