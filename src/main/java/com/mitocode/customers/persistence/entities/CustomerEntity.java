package com.mitocode.customers.persistence.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "clients")
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String status = "Activo";
}
