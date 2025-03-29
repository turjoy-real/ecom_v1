package com.services.orderservice.dtos;


import java.time.LocalDateTime;

import com.services.orderservice.models.Order;

import lombok.Data;

@Data
public class OrderStatusUpdateDTO {
    private String orderNumber;
    private Order.OrderStatus newStatus;
    private String trackingNumber;
    private LocalDateTime estimatedDelivery;
}