package com.services.paymentservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.services.common.enums.PaymentStatus;
import com.services.common.dtos.OrderResponse;

@FeignClient(name = "orderservice")
public interface OrderServiceFeignClient {
        @PostMapping("/api/orders/payment-status/update")
        String updatePaymentStatus(
                        @RequestHeader("Authorization") String token,
                        @RequestParam("orderId") Long orderId,
                        @RequestParam("paymentStatus") PaymentStatus paymentStatus);

        @PostMapping("/api/orders/status/update")
        String updateOrderStatus(
                        @RequestHeader("Authorization") String token,
                        @RequestParam("orderId") Long orderId,
                        @RequestParam("status") String status);

        @GetMapping("/api/orders/admin/{orderId}")
        OrderResponse getOrderById(@RequestHeader("Authorization") String token, @PathVariable("orderId") Long orderId);
}