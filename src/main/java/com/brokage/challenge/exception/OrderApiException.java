package com.brokage.challenge.exception;

public class OrderApiException extends RuntimeException {

    public OrderApiException(String message) {
        super(message);
    }

    public OrderApiException(String message, Throwable cause) {
        super(message, cause);
    }
}