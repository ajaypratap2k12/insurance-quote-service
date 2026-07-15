package com.example.insurance.exception;

import lombok.Getter;

@Getter
public class InsuranceException extends RuntimeException {

    private final String errorCode;

    public InsuranceException(String message) {
        super(message);
        this.errorCode = "INSURANCE_ERROR";
    }

    public InsuranceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public InsuranceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "INSURANCE_ERROR";
    }

    public InsuranceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
