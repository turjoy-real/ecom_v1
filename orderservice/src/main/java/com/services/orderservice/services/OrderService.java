package com.services.orderservice.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.services.orderservice.dtos.OrderResponse;

public interface OrderService {
    OrderResponse createOrderFromCart(@AuthenticationPrincipal Jwt jwt, Long addressId);
    boolean updateOrderStatus(Long orderId, String status);
    boolean updatePaymentStatus(Long orderId, String paymentStatus);
}
