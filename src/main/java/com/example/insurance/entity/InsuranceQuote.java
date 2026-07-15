package com.example.insurance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_quotes", schema = "insurance")
public class InsuranceQuote extends BaseEntity {

    @Column(name = "workflow_id", nullable = false)
    private String workflowId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "vehicle_type", nullable = false)
    private String vehicleType;

    @Column(name = "vehicle_value", precision = 15, scale = 2)
    private BigDecimal vehicleValue;

    @Column(name = "claim_history")
    private Integer claimHistory;

    @Column(name = "premium", precision = 15, scale = 2)
    private BigDecimal premium;

    @Column(name = "discount", precision = 5, scale = 2)
    private BigDecimal discount;

    @Column(name = "risk_category")
    private String riskCategory;

    @Column(name = "status", nullable = false)
    private String status;
}
