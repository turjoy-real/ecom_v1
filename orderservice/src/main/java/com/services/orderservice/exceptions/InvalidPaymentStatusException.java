package com.services.orderservice.exceptions;

public class InvalidPaymentStatusException extends RuntimeException {
    public InvalidPaymentStatusException(String status) {
        super("Invalid payment status: " + status);
    }

    public InvalidPaymentStatusException(String status, Throwable cause) {
        super("Invalid payment status: " + status, cause);
    }
} 