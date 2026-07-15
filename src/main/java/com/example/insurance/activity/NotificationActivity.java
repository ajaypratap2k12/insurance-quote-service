package com.example.insurance.activity;

import com.example.insurance.dto.NotificationResult;
import com.example.insurance.dto.QuoteResponse;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface NotificationActivity {

    NotificationResult sendNotification(QuoteResponse response);
}
