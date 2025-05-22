package com.services.orderservice.events;

import java.time.LocalDateTime;

import com.services.orderservice.models.OrderStatus;

import lombok.Data;

@Data
public class OrderStatusEvent {
    private String orderNumber;
    private String userId;
    private OrderStatus status;
    private String trackingNumber;
    private LocalDateTime estimatedDelivery;
}
