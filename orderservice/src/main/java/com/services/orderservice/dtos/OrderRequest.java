package com.services.orderservice.dtos;

import lombok.Data;

@Data
public class OrderRequest {
    private Long shippingAddressId;
}