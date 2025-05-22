package com.services.orderservice.dtos;

import lombok.Data;

@Data
public class OrderItemRequest {
    private String productId;
    private int quantity;
}