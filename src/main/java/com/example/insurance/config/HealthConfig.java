package com.example.insurance.config;

import io.temporal.client.WorkflowClient;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthConfig {

    @Bean
    public HealthIndicator temporalHealthIndicator(WorkflowClient workflowClient) {
        return () -> {
            try {
                workflowClient.getWorkflowServiceStubs().blockingStub();
                return Health.up().withDetail("temporal", "Connected").build();
            } catch (Exception e) {
                return Health.down().withDetail("temporal", e.getMessage()).build();
            }
        };
    }
}
