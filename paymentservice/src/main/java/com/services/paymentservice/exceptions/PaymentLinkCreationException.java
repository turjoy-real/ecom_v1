package com.services.paymentservice.exceptions;

public class PaymentLinkCreationException extends RuntimeException {
    public PaymentLinkCreationException(String message) {
        super(message);
    }

    public PaymentLinkCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}