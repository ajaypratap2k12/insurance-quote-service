package com.example.insurance.controller;

import com.example.insurance.dto.QuoteRequest;
import com.example.insurance.dto.QuoteResponse;
import com.example.insurance.service.InsuranceQuoteService;
import com.example.insurance.workflow.InsuranceQuoteWorkflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuoteController.class)
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkflowClient workflowClient;

    @MockitoBean
    private InsuranceQuoteService insuranceQuoteService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createQuote_ShouldReturnCreated_WhenValidRequest() throws Exception {
        QuoteRequest request = QuoteRequest.builder()
                .customerName("John Doe")
                .age(30)
                .vehicleType("SEDAN")
                .vehicleValue(new BigDecimal("35000"))
                .claimHistory(0)
                .build();

        QuoteResponse response = QuoteResponse.builder()
                .id(UUID.randomUUID().toString())
                .customerName("John Doe")
                .status("APPROVED")
                .build();

        InsuranceQuoteWorkflow workflow = org.mockito.Mockito.mock(InsuranceQuoteWorkflow.class);
        when(workflowClient.newWorkflowStub(any(Class.class), any(WorkflowOptions.class))).thenReturn(workflow);
        when(workflow.processQuoteRequest(any(QuoteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerName").value("John Doe"));
    }

    @Test
    void createQuote_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        QuoteRequest request = QuoteRequest.builder().build();

        mockMvc.perform(post("/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getQuote_ShouldReturnQuote_WhenExists() throws Exception {
        QuoteResponse response = QuoteResponse.builder()
                .id(UUID.randomUUID().toString())
                .customerName("John Doe")
                .status("APPROVED")
                .build();

        when(insuranceQuoteService.findById(anyString())).thenReturn(Optional.of(response));

        mockMvc.perform(get("/v1/quotes/" + response.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerName").value("John Doe"));
    }

    @Test
    void getQuote_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(insuranceQuoteService.findById(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/quotes/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
