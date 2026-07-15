package com.example.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionResult {

    private String riskScore;
    private String riskCategory;
    private BigDecimal premiumMultiplier;
    private BigDecimal discount;
    private String decision;
    private String reason;
}
