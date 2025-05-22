package com.services.orderservice.dtos;

import java.time.LocalDateTime;

import com.services.orderservice.models.OrderStatus;

import lombok.Data;

@Data
public class OrderStatusUpdateDTO {
    private String orderNumber;
    private OrderStatus newStatus;
    private String trackingNumber;
    private LocalDateTime estimatedDelivery;
}