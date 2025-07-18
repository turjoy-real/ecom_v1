package com.services.common.dtos;

import lombok.Data;

import java.util.List;

@Data
public class CartResponse {
    private String userId;
    private List<CartItemDTO> items;
    private double total;
}