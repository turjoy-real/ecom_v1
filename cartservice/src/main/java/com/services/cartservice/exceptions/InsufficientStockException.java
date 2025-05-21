package com.services.cartservice.exceptions;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String productId, int quantity) {
        super("Insufficient stock for product ID: " + productId + ". Requested quantity: " + quantity);
    }
}