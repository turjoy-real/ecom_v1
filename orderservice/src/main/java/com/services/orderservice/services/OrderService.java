package com.services.orderservice.services;

import org.springframework.security.core.Authentication;

import com.services.orderservice.dtos.OrderResponse;

public interface OrderService {
    OrderResponse createOrderFromCart(Authentication authentication, Long addressId);

    boolean updateOrderStatus(Long orderId, String status);

    boolean updatePaymentStatus(Long orderId, String paymentStatus);
}
