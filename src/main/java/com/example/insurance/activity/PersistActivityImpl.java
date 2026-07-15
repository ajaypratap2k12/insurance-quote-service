package com.example.insurance.activity;

import com.example.insurance.dto.QuoteResponse;
import com.example.insurance.service.InsuranceQuoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PersistActivityImpl implements PersistActivity {

    private final InsuranceQuoteService insuranceQuoteService;

    public PersistActivityImpl(InsuranceQuoteService insuranceQuoteService) {
        this.insuranceQuoteService = insuranceQuoteService;
    }

    @Override
    public QuoteResponse saveQuote(QuoteResponse response) {
        log.info("Persisting quote for customer: {}", response.getCustomerName());

        QuoteResponse savedResponse = insuranceQuoteService.save(response);

        log.info("Quote persisted successfully with id: {}", savedResponse.getId());
        return savedResponse;
    }
}
