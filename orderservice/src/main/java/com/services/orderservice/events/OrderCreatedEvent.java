package com.services.orderservice.events;


import lombok.Data;

import java.util.List;

import com.services.orderservice.dtos.OrderItemDTO;

@Data
public class OrderCreatedEvent {
    private String orderNumber;
    private String userId;
    private Double totalAmount;
    private List<OrderItemDTO> items;
    private String paymentMethod;
}