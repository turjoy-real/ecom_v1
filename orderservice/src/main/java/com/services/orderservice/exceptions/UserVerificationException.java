package com.services.orderservice.exceptions;

public class UserVerificationException extends RuntimeException {
    public UserVerificationException(String message) {
        super(message);
    }

    public UserVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}