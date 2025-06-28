package com.services.orderservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.services.common.dtos.CreatePaymentLinkRequestDto;

@FeignClient(name = "paymentservice")
public interface PaymentClient {
    @PostMapping("/api/payment")
    String generatePaymentLink(
            @RequestHeader("Authorization") String token,
            @RequestBody CreatePaymentLinkRequestDto request);
}