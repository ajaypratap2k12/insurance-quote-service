package com.example.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {

    private String id;
    private String workflowId;
    private String customerName;
    private Integer age;
    private String vehicleType;
    private BigDecimal vehicleValue;
    private Integer claimHistory;
    private BigDecimal premiumAmount;
    private BigDecimal discount;
    private String riskCategory;
    private String status;
    private LocalDateTime createdDate;
}
