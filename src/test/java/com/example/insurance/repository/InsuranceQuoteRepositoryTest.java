package com.example.insurance.repository;

import com.example.insurance.entity.InsuranceQuote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InsuranceQuoteRepositoryTest {

    @Autowired
    private InsuranceQuoteRepository repository;

    @Test
    void save_ShouldPersistQuote() {
        InsuranceQuote quote = InsuranceQuote.builder()
                .workflowId("wf-test-1")
                .customerName("Test User")
                .age(25)
                .vehicleType("SEDAN")
                .vehicleValue(new BigDecimal("25000"))
                .claimHistory(0)
                .premium(new BigDecimal("500.00"))
                .discount(BigDecimal.ZERO)
                .riskCategory("STANDARD")
                .status("APPROVED")
                .build();

        InsuranceQuote saved = repository.save(quote);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomerName()).isEqualTo("Test User");
    }

    @Test
    void findByWorkflowId_ShouldReturnQuote() {
        InsuranceQuote quote = InsuranceQuote.builder()
                .workflowId("wf-find-test")
                .customerName("Find Me")
                .age(35)
                .vehicleType("SUV")
                .vehicleValue(new BigDecimal("45000"))
                .claimHistory(1)
                .premium(new BigDecimal("800.00"))
                .discount(new BigDecimal("0.10"))
                .riskCategory("MEDIUM")
                .status("APPROVED")
                .build();

        repository.save(quote);

        Optional<InsuranceQuote> found = repository.findByWorkflowId("wf-find-test");

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerName()).isEqualTo("Find Me");
    }

    @Test
    void findByWorkflowId_ShouldReturnEmpty_WhenNotExists() {
        Optional<InsuranceQuote> found = repository.findByWorkflowId("non-existent");

        assertThat(found).isEmpty();
    }
}
