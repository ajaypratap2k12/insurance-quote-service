package com.example.insurance.activity;

import com.example.insurance.dto.NotificationResult;
import com.example.insurance.dto.QuoteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationActivityImpl implements NotificationActivity {

    @Override
    public NotificationResult sendNotification(QuoteResponse response) {
        log.info("Sending notification for quote: {} to customer: {}", response.getId(), response.getCustomerName());

        NotificationResult result = NotificationResult.builder()
                .sent(true)
                .channel("EMAIL")
                .message("Quote " + response.getId() + " has been processed for " + response.getCustomerName())
                .build();

        log.info("Notification sent for quote {} to customer {}", response.getId(), response.getCustomerName());
        return result;
    }
}
