package com.example.insurance.activity;

import com.example.insurance.dto.QuoteResponse;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface PersistActivity {

    QuoteResponse saveQuote(QuoteResponse response);
}
