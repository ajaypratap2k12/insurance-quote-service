package com.example.insurance.activity;

import com.example.insurance.dto.QuoteRequest;
import com.example.insurance.dto.ValidationResult;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface ValidateActivity {

    ValidationResult validate(QuoteRequest request);
}
