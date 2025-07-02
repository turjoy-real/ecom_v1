package com.services.paymentservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.services.common.enums.PaymentStatus;

@FeignClient(name = "orderservice")
public interface OrderServiceFeignClient {
    @PostMapping("/api/orders/payment-status/update")
    String updatePaymentStatus(
        @RequestHeader("Authorization") String token,
        @RequestParam("orderId") Long orderId,
        @RequestParam("paymentStatus") PaymentStatus paymentStatus
    );
} 