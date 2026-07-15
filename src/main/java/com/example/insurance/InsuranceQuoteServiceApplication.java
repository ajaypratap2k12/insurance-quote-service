package com.example.insurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InsuranceQuoteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceQuoteServiceApplication.class, args);
    }
}
