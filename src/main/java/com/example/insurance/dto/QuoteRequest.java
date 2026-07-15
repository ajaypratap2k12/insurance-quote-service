package com.example.insurance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequest {

    @NotNull(message = "Customer name is required")
    private String customerName;

    @NotNull(message = "Age is required")
    private Integer age;

    @NotNull(message = "Vehicle type is required")
    private String vehicleType;

    @NotNull(message = "Vehicle value is required")
    private BigDecimal vehicleValue;

    private Integer claimHistory;
}
