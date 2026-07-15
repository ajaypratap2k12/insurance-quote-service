package com.example.insurance.workflow;

import com.example.insurance.dto.QuoteRequest;
import com.example.insurance.dto.QuoteResponse;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface InsuranceQuoteWorkflow {

    String WORKFLOW_ID = "insurance-quote-workflow";
    String TASK_QUEUE = "INSURANCE_TASK_QUEUE";

    @WorkflowMethod
    QuoteResponse processQuoteRequest(QuoteRequest request);
}
