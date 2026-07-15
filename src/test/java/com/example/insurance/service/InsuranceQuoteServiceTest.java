package com.example.insurance.service;

import com.example.insurance.dto.QuoteResponse;
import com.example.insurance.entity.InsuranceQuote;
import com.example.insurance.mapper.InsuranceQuoteMapper;
import com.example.insurance.repository.InsuranceQuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsuranceQuoteServiceTest {

    @Mock
    private InsuranceQuoteRepository repository;

    @Mock
    private InsuranceQuoteMapper mapper;

    @InjectMocks
    private InsuranceQuoteServiceImpl service;

    private InsuranceQuote entity;
    private QuoteResponse dto;

    @BeforeEach
    void setUp() {
        entity = InsuranceQuote.builder()
                .workflowId("wf-123")
                .customerName("John Doe")
                .age(30)
                .vehicleType("SEDAN")
                .vehicleValue(new BigDecimal("35000"))
                .claimHistory(0)
                .premium(new BigDecimal("720.00"))
                .discount(new BigDecimal("0.25"))
                .riskCategory("LOW_RISK")
                .status("APPROVED")
                .build();

        dto = QuoteResponse.builder()
                .id(UUID.randomUUID().toString())
                .workflowId("wf-123")
                .customerName("John Doe")
                .age(30)
                .vehicleType("SEDAN")
                .vehicleValue(new BigDecimal("35000"))
                .claimHistory(0)
                .premiumAmount(new BigDecimal("720.00"))
                .discount(new BigDecimal("0.25"))
                .riskCategory("LOW_RISK")
                .status("APPROVED")
                .createdDate(LocalDateTime.now())
                .build();
    }

    @Test
    void save_ShouldReturnSavedQuote() {
        when(mapper.toEntity(any(QuoteResponse.class))).thenReturn(entity);
        when(repository.save(any(InsuranceQuote.class))).thenReturn(entity);
        when(mapper.toDto(any(InsuranceQuote.class))).thenReturn(dto);

        QuoteResponse result = service.save(dto);

        assertThat(result).isNotNull();
        assertThat(result.getCustomerName()).isEqualTo("John Doe");
        assertThat(result.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void findById_ShouldReturnQuote_WhenExists() {
        when(repository.findById(any(UUID.class))).thenReturn(Optional.of(entity));
        when(mapper.toDto(any(InsuranceQuote.class))).thenReturn(dto);

        Optional<QuoteResponse> result = service.findById(dto.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getCustomerName()).isEqualTo("John Doe");
    }

    @Test
    void findByWorkflowId_ShouldReturnQuote_WhenExists() {
        when(repository.findByWorkflowId("wf-123")).thenReturn(Optional.of(entity));
        when(mapper.toDto(any(InsuranceQuote.class))).thenReturn(dto);

        Optional<QuoteResponse> result = service.findByWorkflowId("wf-123");

        assertThat(result).isPresent();
        assertThat(result.get().getWorkflowId()).isEqualTo("wf-123");
    }
}
