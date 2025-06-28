package com.services.orderservice.exceptions;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String status) {
        super("Invalid order status: " + status);
    }

    public InvalidOrderStatusException(String status, Throwable cause) {
        super("Invalid order status: " + status, cause);
    }
} 