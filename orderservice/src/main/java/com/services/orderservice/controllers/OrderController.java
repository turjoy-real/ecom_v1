package com.services.orderservice.controllers;

import com.services.orderservice.dtos.*;
import com.services.orderservice.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false, defaultValue = "orderDate") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        String userId = authentication.getName();
        return ResponseEntity.ok(orderService.getUserOrders(userId, status, paymentStatus, sortBy, sortDirection));
    }

    @GetMapping("/my/analytics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyOrderAnalytics(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(orderService.getOrderAnalytics(userId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @PathVariable String userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false, defaultValue = "orderDate") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(orderService.getUserOrders(userId, status, paymentStatus, sortBy, sortDirection));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    // @PatchMapping("/{orderId}/payment-status")
    // public ResponseEntity<OrderResponse> updatePaymentStatus(
    // @PathVariable Long orderId,
    // @RequestParam String paymentStatus) {
    // return ResponseEntity.ok(orderService.updatePaymentStatus(orderId,
    // paymentStatus));
    // }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    // New tracking endpoints
    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<OrderTrackingResponse> getOrderTracking(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderTracking(orderId));
    }

    @PostMapping("/{orderId}/tracking")
    public ResponseEntity<OrderTrackingResponse> updateOrderTracking(
            @PathVariable Long orderId,
            @RequestParam String trackingNumber,
            @RequestParam String carrier) {
        return ResponseEntity.ok(orderService.updateOrderTracking(orderId, trackingNumber, carrier));
    }

    // New return request endpoints
    @PostMapping("/{orderId}/return")
    public ResponseEntity<ReturnRequestResponse> createReturnRequest(
            @PathVariable Long orderId,
            @RequestBody ReturnRequestDTO request) {
        return ResponseEntity.ok(orderService.createReturnRequest(orderId, request));
    }

    @GetMapping("/returns/{returnId}")
    public ResponseEntity<ReturnRequestResponse> getReturnRequest(@PathVariable Long returnId) {
        return ResponseEntity.ok(orderService.getReturnRequest(returnId));
    }

    @PatchMapping("/returns/{returnId}/status")
    public ResponseEntity<ReturnRequestResponse> updateReturnStatus(
            @PathVariable Long returnId,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateReturnStatus(returnId, status));
    }

    @GetMapping("/returns")
    public ResponseEntity<List<ReturnRequestResponse>> getReturnRequestsByStatus(
            @RequestParam(required = false, defaultValue = "PENDING") String status) {
        return ResponseEntity.ok(orderService.getReturnRequestsByStatus(status));
    }

    // New analytics endpoint
    @GetMapping("/user/{userId}/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrderAnalytics(@PathVariable String userId) {
        return ResponseEntity.ok(orderService.getOrderAnalytics(userId));
    }
}