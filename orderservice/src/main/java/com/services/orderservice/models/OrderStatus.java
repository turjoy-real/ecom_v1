package com.services.orderservice.models;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAYMENT_VERIFIED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}