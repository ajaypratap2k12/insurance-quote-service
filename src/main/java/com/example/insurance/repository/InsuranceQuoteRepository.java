package com.example.insurance.repository;

import com.example.insurance.entity.InsuranceQuote;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InsuranceQuoteRepository extends BaseRepository<InsuranceQuote> {

    Optional<InsuranceQuote> findByWorkflowId(String workflowId);

    Optional<InsuranceQuote> findByCustomerName(String customerName);
}
