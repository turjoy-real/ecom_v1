package com.services.orderservice.services;

import java.util.List;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.services.common.enums.PaymentStatus;
import com.services.orderservice.dtos.OrderResponse;

public interface OrderService {
    OrderResponse createOrderFromCart(@AuthenticationPrincipal Jwt jwt, Long addressId);
    boolean updateOrderStatus(Long orderId, String status);
    boolean updatePaymentStatus(Long orderId, PaymentStatus paymentStatus);
    List<OrderResponse> getOrdersByUserId(String userId);
    OrderResponse getOrderByIdForUser(Long orderId, String userId);
}
