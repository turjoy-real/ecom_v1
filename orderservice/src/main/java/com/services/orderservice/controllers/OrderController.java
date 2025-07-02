package com.services.orderservice.controllers;

import com.services.orderservice.dtos.*;
import com.services.orderservice.services.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrderFromCart(jwt, request.getShippingAddressId()));
    }

    @PostMapping("/status/update")
    public ResponseEntity<?> updateOrderStatus(@RequestParam Long orderId, @RequestParam String status) {
        boolean success = orderService.updateOrderStatus(orderId, status);
        if (success) {
            return ResponseEntity.ok("Order status updated and notification sent");
        } else {
            return ResponseEntity.badRequest().body("Order not found");
        }
    }

    @PostMapping("/payment-status/update")
    public ResponseEntity<?> updatePaymentStatus(@RequestParam Long orderId, @RequestParam String paymentStatus) {
        boolean success = orderService.updatePaymentStatus(orderId, paymentStatus);
        if (success) {
            return ResponseEntity.ok("Payment status updated");
        } else {
            return ResponseEntity.badRequest().body("Order not found");
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

}