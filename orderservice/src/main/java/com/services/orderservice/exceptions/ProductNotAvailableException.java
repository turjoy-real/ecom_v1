package com.services.orderservice.exceptions;

public class ProductNotAvailableException extends RuntimeException {

    public ProductNotAvailableException(String message) {
        super(message);
    }

    public ProductNotAvailableException(Long productId, int requestedQuantity) {
        super(String.format("Product with ID %d is not available in quantity %d", productId, requestedQuantity));
    }

    public ProductNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}