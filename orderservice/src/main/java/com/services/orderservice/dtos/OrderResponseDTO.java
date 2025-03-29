package com.services.orderservice.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.services.orderservice.models.Order;

@Data
public class OrderResponseDTO {
    private Long id;
    private String orderNumber;
    private String userId;
    private LocalDateTime orderDate;
    private Order.OrderStatus status;
    private List<OrderItemDTO> items;
    private Double totalAmount;
    private String shippingAddress;
    private String paymentMethod;
    private String trackingNumber;
    private LocalDateTime estimatedDelivery;
}
