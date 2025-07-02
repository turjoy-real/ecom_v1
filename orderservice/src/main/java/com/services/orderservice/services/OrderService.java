package com.services.orderservice.services;

import java.util.List;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.services.orderservice.dtos.OrderResponse;

public interface OrderService {
    OrderResponse createOrderFromCart(@AuthenticationPrincipal Jwt jwt, Long addressId);
    boolean updateOrderStatus(Long orderId, String status);
    boolean updatePaymentStatus(Long orderId, String paymentStatus);
    List<OrderResponse> getOrdersByUserId(String userId);
}
