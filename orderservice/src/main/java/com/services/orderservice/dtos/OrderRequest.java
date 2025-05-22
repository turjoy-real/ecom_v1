package com.services.orderservice.dtos;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String userId;
    private List<OrderItemRequest> items;
    private String shippingAddress;
}