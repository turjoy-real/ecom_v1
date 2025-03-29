package com.services.cartservice.dtos;

import lombok.Data;

@Data
public class CartItemDTO {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
}
