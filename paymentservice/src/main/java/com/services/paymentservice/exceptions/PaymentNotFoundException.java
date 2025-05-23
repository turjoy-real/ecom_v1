package com.services.paymentservice.exceptions;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long paymentId) {
        super("Payment not found with ID: " + paymentId);
    }

    public PaymentNotFoundException(String message) {
        super(message);
    }
}