package com.services.common.dtos;

import com.services.common.enums.OrderStatus;
import com.services.common.enums.PaymentStatus;
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
    private LocalDateTime updatedAt;
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