package com.mitocode.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import com.mitocode.iam.persistence.entities.UserEntity;
import com.mitocode.financialinstitutions.persistence.entities.FinancialProductEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Entity
@Table(name = "simulations")
public class SimulationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserEntity owner;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_product_id")
    private FinancialProductEntity financialProduct;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "product_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> productSnapshot;
    private String code;
    private Integer clientId;
    private Integer vehicleId;
    private Integer entityId;
    private String currency = "PEN";
    private BigDecimal vehiclePrice;
    private BigDecimal downPayment;
    private BigDecimal downPaymentPercent = new BigDecimal("20");
    private BigDecimal financedAmount;
    private Integer term;
    private BigDecimal tea;
    private BigDecimal tem;
    @Column(name="cok_tea") private BigDecimal cokTea;
    @Column(name="cok_tem") private BigDecimal cokTem;
    private Integer paymentDay = 5;
    private LocalDate disbursementDate;
    @Column(name="first_payment_date") private LocalDate firstPaymentDate;
    private String graceType = "none";
    private Integer graceMonths = 0;
    private Boolean balloonEnabled = false;
    @Column(name="balloon_percent") private BigDecimal balloonPercent = BigDecimal.ZERO;
    private BigDecimal balloonAmount = BigDecimal.ZERO;
    private BigDecimal insuranceDisbursement = BigDecimal.ZERO;
    private BigDecimal insuranceVehicle = BigDecimal.ZERO;
    private BigDecimal monthlyFee = BigDecimal.ZERO;
    private BigDecimal adminCost = BigDecimal.ZERO;
    private BigDecimal notaryCost = BigDecimal.ZERO;
    private BigDecimal otherCharges = BigDecimal.ZERO;
    private BigDecimal monthlyPayment = BigDecimal.ZERO;
    private BigDecimal van = BigDecimal.ZERO;
    private BigDecimal tir = BigDecimal.ZERO;
    private BigDecimal tcea = BigDecimal.ZERO;
    @Column(name="total_interest") private BigDecimal totalInterest = BigDecimal.ZERO;
    @Column(name="total_insurance") private BigDecimal totalInsurance = BigDecimal.ZERO;
    @Column(name="total_fees") private BigDecimal totalFees = BigDecimal.ZERO;
    @Column(name="total_payment") private BigDecimal totalPayment = BigDecimal.ZERO;
    private String status = "Borrador";
    private LocalDate createdAt;
    @Column(nullable = false)
    private Boolean archived = false;
    @Column(name = "created_at_ts", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAtTimestamp;
}
