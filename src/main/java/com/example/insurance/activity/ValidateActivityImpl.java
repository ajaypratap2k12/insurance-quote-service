package com.example.insurance.activity;

import com.example.insurance.dto.QuoteRequest;
import com.example.insurance.dto.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ValidateActivityImpl implements ValidateActivity {

    @Override
    public ValidationResult validate(QuoteRequest request) {
        log.info("Validating quote request for customer: {}", request.getCustomerName());

        List<String> errors = new ArrayList<>();

        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            errors.add("Customer name is required");
        }
        if (request.getAge() == null) {
            errors.add("Age is required");
        }
        if (request.getVehicleType() == null || request.getVehicleType().isBlank()) {
            errors.add("Vehicle type is required");
        }
        if (request.getVehicleValue() == null) {
            errors.add("Vehicle value is required");
        }

        ValidationResult result = ValidationResult.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .message(errors.isEmpty() ? "Validation passed" : "Validation failed with " + errors.size() + " errors")
                .build();

        log.info("Validation result for customer {}: {}", request.getCustomerName(), result.getMessage());
        return result;
    }
}
