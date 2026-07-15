package com.example.insurance.activity;

import com.example.insurance.dto.DecisionResult;
import com.example.insurance.dto.QuoteRequest;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface DecisionActivity {

    DecisionResult evaluateRisk(QuoteRequest request);
}
