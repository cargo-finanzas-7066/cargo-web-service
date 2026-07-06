package com.mitocode.customers.persistence.entities;

import jakarta.persistence.*;
import lombok.Data;
import com.mitocode.iam.persistence.entities.UserEntity;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "clients")
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserEntity owner;
    private String docType;
    private String docNumber;
    private String names;
    private String surnames;
    private String email;
    private String phone;
    private String address;
    @Column(precision=19, scale=2) private BigDecimal monthlyIncome;
    private String occupation;
    private String status = "Activo";
    @Column(nullable = false)
    private Boolean archived = false;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
