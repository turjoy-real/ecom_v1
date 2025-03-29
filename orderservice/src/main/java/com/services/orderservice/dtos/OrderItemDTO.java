package com.services.orderservice.dtos;

import lombok.Data;

@Data
public class OrderItemDTO {
    private String productId;
    private String productName;
    private Double price;
    private Integer quantity;
}
