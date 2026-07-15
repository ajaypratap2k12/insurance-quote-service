package com.example.insurance.config;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TemporalConfig {

    @Value("${temporal.connection.target:localhost:7233}")
    private String temporalTarget;

    @Value("${temporal.connection.namespace:default}")
    private String namespace;

    @Bean
    public WorkflowServiceStubs workflowServiceStrokes() {
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalTarget)
                .build();
        return WorkflowServiceStubs.newServiceStubs(options);
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        WorkflowClientOptions options = WorkflowClientOptions.newBuilder()
                .setNamespace(namespace)
                .build();
        return WorkflowClient.newInstance(serviceStubs, options);
    }
}
