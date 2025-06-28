package com.services.orderservice.exceptions;

public class CartServiceException extends RuntimeException {
    public CartServiceException(String message) {
        super(message);
    }

    public CartServiceException(String message, Throwable cause) {
        super(message, cause);
    }
} 