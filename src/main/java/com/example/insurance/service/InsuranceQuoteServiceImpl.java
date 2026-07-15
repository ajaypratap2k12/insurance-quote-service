package com.example.insurance.service;

import com.example.insurance.dto.QuoteResponse;
import com.example.insurance.entity.InsuranceQuote;
import com.example.insurance.mapper.InsuranceQuoteMapper;
import com.example.insurance.repository.InsuranceQuoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class InsuranceQuoteServiceImpl implements InsuranceQuoteService {

    private final InsuranceQuoteRepository repository;
    private final InsuranceQuoteMapper mapper;

    public InsuranceQuoteServiceImpl(InsuranceQuoteRepository repository, InsuranceQuoteMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public QuoteResponse save(QuoteResponse quoteResponse) {
        log.info("Saving insurance quote for customer: {}", quoteResponse.getCustomerName());

        InsuranceQuote entity = mapper.toEntity(quoteResponse);
        InsuranceQuote savedEntity = repository.save(entity);

        log.info("Insurance quote saved with id: {}", savedEntity.getId());
        return mapper.toDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QuoteResponse> findById(String id) {
        log.info("Finding insurance quote by id: {}", id);
        return repository.findById(java.util.UUID.fromString(id))
                .map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QuoteResponse> findByWorkflowId(String workflowId) {
        log.info("Finding insurance quote by workflowId: {}", workflowId);
        return repository.findByWorkflowId(workflowId)
                .map(mapper::toDto);
    }
}
