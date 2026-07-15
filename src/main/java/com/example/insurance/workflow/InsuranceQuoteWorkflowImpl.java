package com.example.insurance.workflow;

import com.example.insurance.activity.DecisionActivity;
import com.example.insurance.activity.NotificationActivity;
import com.example.insurance.activity.PersistActivity;
import com.example.insurance.activity.ValidateActivity;
import com.example.insurance.dto.DecisionResult;
import com.example.insurance.dto.NotificationResult;
import com.example.insurance.dto.QuoteRequest;
import com.example.insurance.dto.QuoteResponse;
import com.example.insurance.dto.ValidationResult;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
public class InsuranceQuoteWorkflowImpl implements InsuranceQuoteWorkflow {

    private final ActivityOptions defaultActivityOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .setRetryOptions(RetryOptions.newBuilder()
                    .setMaximumAttempts(3)
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setMaximumInterval(Duration.ofSeconds(10))
                    .setBackoffCoefficient(2.0)
                    .build())
            .build();

    private final ValidateActivity validateActivity = Workflow.newActivityStub(
            ValidateActivity.class, defaultActivityOptions);

    private final DecisionActivity decisionActivity = Workflow.newActivityStub(
            DecisionActivity.class, defaultActivityOptions);

    private final PersistActivity persistActivity = Workflow.newActivityStub(
            PersistActivity.class, defaultActivityOptions);

    private final NotificationActivity notificationActivity = Workflow.newActivityStub(
            NotificationActivity.class, defaultActivityOptions);

    @Override
    public QuoteResponse processQuoteRequest(QuoteRequest request) {
        String workflowId = Workflow.getInfo().getWorkflowId();
        log.info("Starting insurance quote workflow {} for customer: {}", workflowId, request.getCustomerName());

        // Step 1: Validate the request
        log.info("Step 1: Validating quote request");
        ValidationResult validationResult = validateActivity.validate(request);
        if (!validationResult.isValid()) {
            log.warn("Validation failed for customer {}: {}", request.getCustomerName(), validationResult.getErrors());
            return QuoteResponse.builder()
                    .workflowId(workflowId)
                    .customerName(request.getCustomerName())
                    .status("VALIDATION_FAILED")
                    .createdDate(LocalDateTime.now())
                    .build();
        }
        log.info("Step 1 completed: Validation passed");

        // Step 2: Evaluate risk and make decision
        log.info("Step 2: Evaluating risk");
        DecisionResult decisionResult = decisionActivity.evaluateRisk(request);
        log.info("Step 2 completed: Decision - {}", decisionResult.getDecision());

        // Step 3: Persist the quote
        log.info("Step 3: Persisting quote");
        QuoteResponse quoteResponse = QuoteResponse.builder()
                .workflowId(workflowId)
                .customerName(request.getCustomerName())
                .age(request.getAge())
                .vehicleType(request.getVehicleType())
                .vehicleValue(request.getVehicleValue())
                .claimHistory(request.getClaimHistory())
                .premiumAmount(calculatePremium(request, decisionResult))
                .discount(decisionResult.getDiscount())
                .riskCategory(decisionResult.getRiskCategory())
                .status(decisionResult.getDecision())
                .createdDate(LocalDateTime.now())
                .build();

        QuoteResponse persistedResponse = persistActivity.saveQuote(quoteResponse);
        log.info("Step 3 completed: Quote {} persisted", persistedResponse.getId());

        // Step 4: Send notification
        log.info("Step 4: Sending notification");
        NotificationResult notificationResult = notificationActivity.sendNotification(persistedResponse);
        log.info("Step 4 completed: Notification sent via {}", notificationResult.getChannel());

        log.info("Insurance quote workflow {} completed for customer: {}", workflowId, request.getCustomerName());
        return persistedResponse;
    }

    private BigDecimal calculatePremium(QuoteRequest request, DecisionResult decisionResult) {
        BigDecimal basePremium = BigDecimal.valueOf(100.00);
        return basePremium.multiply(decisionResult.getPremiumMultiplier());
    }
}
