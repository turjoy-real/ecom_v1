package com.services.orderservice.services;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.services.common.dtos.OrderResponse;
import com.services.common.enums.PaymentStatus;

public interface OrderService {
    OrderResponse createOrderFromCart(@AuthenticationPrincipal Jwt jwt, Long addressId);

    boolean updateOrderStatus(@AuthenticationPrincipal Jwt jwt, Long orderId, String status);

    boolean updatePaymentStatus(@AuthenticationPrincipal Jwt jwt, Long orderId, PaymentStatus paymentStatus);

    List<OrderResponse> getOrdersByUserId(String userId);

    OrderResponse getOrderByIdForUser(Long orderId, String userId);

    OrderResponse getOrderById(Long orderId);
}
