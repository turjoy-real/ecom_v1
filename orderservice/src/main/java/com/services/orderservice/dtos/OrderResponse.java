package com.services.orderservice.dtos;

import com.services.common.enums.PaymentStatus;
import com.services.orderservice.models.OrderStatus;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private String id;
    private String userId;
    private List<OrderItemResponse> items;
    private double totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private String shippingAddressId;
    private String paymentMethod;
    private String paymentLink;

    @Data
    public static class OrderItemResponse {
        private String productId;
        private String productName;
        private double price;
        private int quantity;
        private double subtotal;
    }
}