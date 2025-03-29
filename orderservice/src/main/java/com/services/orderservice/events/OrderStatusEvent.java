package com.services.orderservice.events;

import java.time.LocalDateTime;

import com.services.orderservice.models.Order;

import lombok.Data;

@Data
public class OrderStatusEvent {
    private String orderNumber;
    private String userId;
    private Order.OrderStatus status;
    private String trackingNumber;
    private LocalDateTime estimatedDelivery;
}
