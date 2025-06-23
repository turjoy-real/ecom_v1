package com.services.orderservice.controllers;

import com.services.orderservice.dtos.*;
import com.services.orderservice.services.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(Authentication authentication, @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrderFromCart(authentication, request.getShippingAddressId()));
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

}