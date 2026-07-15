package com.example.insurance.service;

import com.example.insurance.dto.QuoteResponse;

import java.util.Optional;

public interface InsuranceQuoteService {

    QuoteResponse save(QuoteResponse quoteResponse);

    Optional<QuoteResponse> findById(String id);

    Optional<QuoteResponse> findByWorkflowId(String workflowId);
}
