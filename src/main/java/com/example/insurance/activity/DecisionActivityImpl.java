package com.example.insurance.activity;

import com.example.insurance.dto.DecisionResult;
import com.example.insurance.dto.QuoteRequest;
import com.example.insurance.rules.GoRulesEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DecisionActivityImpl implements DecisionActivity {

    private final GoRulesEngine goRulesEngine;

    public DecisionActivityImpl(GoRulesEngine goRulesEngine) {
        this.goRulesEngine = goRulesEngine;
    }

    @Override
    public DecisionResult evaluateRisk(QuoteRequest request) {
        log.info("Evaluating risk for customer: {}", request.getCustomerName());

        // Run eligibility check
        Map<String, Object> eligibilityContext = buildEligibilityContext(request);
        Map<String, Object> eligibilityResult = goRulesEngine.evaluate("eligibility.json", eligibilityContext);

        if (Boolean.FALSE.equals(eligibilityResult.get("eligible"))) {
            return DecisionResult.builder()
                    .riskScore("N/A")
                    .riskCategory("INELIGIBLE")
                    .premiumMultiplier(BigDecimal.ZERO)
                    .discount(BigDecimal.ZERO)
                    .decision("REJECTED")
                    .reason((String) eligibilityResult.get("reason"))
                    .build();
        }

        // Run pricing/risk assessment
        Map<String, Object> pricingContext = buildPricingContext(request);
        Map<String, Object> pricingResult = goRulesEngine.evaluate("pricing.json", pricingContext);

        // Run discount calculation
        Map<String, Object> discountContext = buildDiscountContext(request, pricingResult);
        Map<String, Object> discountResult = goRulesEngine.evaluate("discount.json", discountContext);

        // Build final decision result
        String riskScore = (String) pricingResult.getOrDefault("riskScore", "MEDIUM");
        String riskCategory = (String) pricingResult.getOrDefault("riskCategory", "STANDARD");
        BigDecimal baseMultiplier = new BigDecimal(pricingResult.getOrDefault("baseMultiplier", 1.0).toString());
        BigDecimal discountPercentage = new BigDecimal(discountResult.getOrDefault("discountPercentage", 0).toString());

        // Apply discount to multiplier
        BigDecimal finalMultiplier = baseMultiplier.multiply(BigDecimal.ONE.subtract(discountPercentage));

        DecisionResult result = DecisionResult.builder()
                .riskScore(riskScore)
                .riskCategory(riskCategory)
                .premiumMultiplier(finalMultiplier)
                .discount(discountPercentage)
                .decision("APPROVED")
                .reason(String.format("Risk: %s, Discount: %s%%", riskCategory, discountPercentage.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString()))
                .build();

        log.info("Risk evaluation result for customer {}: {}", request.getCustomerName(), result.getDecision());
        return result;
    }

    private Map<String, Object> buildEligibilityContext(QuoteRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("age", request.getAge());
        context.put("vehicleType", request.getVehicleType());
        context.put("vehicleValue", request.getVehicleValue());
        return context;
    }

    private Map<String, Object> buildPricingContext(QuoteRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("age", request.getAge());
        context.put("vehicleType", request.getVehicleType());
        context.put("vehicleValue", request.getVehicleValue());
        context.put("claimHistory", request.getClaimHistory());
        return context;
    }

    private Map<String, Object> buildDiscountContext(QuoteRequest request, Map<String, Object> pricingResult) {
        Map<String, Object> context = new HashMap<>();
        context.put("premium", pricingResult.get("premium"));
        context.put("customerName", request.getCustomerName());
        context.put("claimHistory", request.getClaimHistory());
        return context;
    }
}
