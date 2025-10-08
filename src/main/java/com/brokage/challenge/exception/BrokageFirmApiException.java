package com.brokage.challenge.exception;

public class BrokageFirmApiException extends RuntimeException {
    
    public BrokageFirmApiException(String message) {
        super(message);
    }
    
    public BrokageFirmApiException(String message, Throwable cause) {
        super(message, cause);
    }
}