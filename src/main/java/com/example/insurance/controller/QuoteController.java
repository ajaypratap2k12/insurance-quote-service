package com.example.insurance.controller;

import com.example.insurance.dto.ApiResponse;
import com.example.insurance.dto.QuoteRequest;
import com.example.insurance.dto.QuoteResponse;
import com.example.insurance.service.InsuranceQuoteService;
import com.example.insurance.workflow.InsuranceQuoteWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/quotes")
@RequiredArgsConstructor
@Tag(name = "Insurance Quote", description = "Insurance Quote Management")
public class QuoteController implements BaseController {

    private final WorkflowClient workflowClient;
    private final InsuranceQuoteService insuranceQuoteService;

    @PostMapping
    @Operation(summary = "Create a new insurance quote", description = "Starts a Temporal workflow to process the insurance quote request")
    public ResponseEntity<ApiResponse<QuoteResponse>> createQuote(@Valid @RequestBody QuoteRequest request) {
        log.info("Received quote request for customer: {}", request.getCustomerName());

        String workflowId = InsuranceQuoteWorkflow.WORKFLOW_ID + "-" + UUID.randomUUID();

        WorkflowOptions workflowOptions = WorkflowOptions.newBuilder()
                .setTaskQueue(InsuranceQuoteWorkflow.TASK_QUEUE)
                .setWorkflowId(workflowId)
                .build();

        InsuranceQuoteWorkflow workflow = workflowClient.newWorkflowStub(
                InsuranceQuoteWorkflow.class, workflowOptions);

        QuoteResponse response = workflow.processQuoteRequest(request);

        log.info("Workflow {} completed for customer: {}", workflowId, request.getCustomerName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quote processed successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get insurance quote by ID", description = "Retrieves a stored insurance quote by its ID")
    public ResponseEntity<ApiResponse<QuoteResponse>> getQuote(@PathVariable String id) {
        log.info("Received request to get quote by id: {}", id);

        return insuranceQuoteService.findById(id)
                .map(quote -> ResponseEntity.ok(ApiResponse.success("Quote retrieved successfully", quote)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Quote not found with id: " + id, "NOT_FOUND")));
    }
}
