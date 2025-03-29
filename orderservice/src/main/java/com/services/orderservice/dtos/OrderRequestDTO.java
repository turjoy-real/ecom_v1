package com.services.orderservice.dtos;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {
    private String userId;
    private List<OrderItemDTO> items;
    private String shippingAddress;
    private String paymentMethod;
}