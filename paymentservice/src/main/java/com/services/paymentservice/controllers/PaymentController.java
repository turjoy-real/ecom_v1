package com.services.paymentservice.controllers;

import com.services.paymentservice.dtos.CreatePaymentLinkRequestDto;
import com.services.paymentservice.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<String> createPaymentLink(@RequestBody CreatePaymentLinkRequestDto request) {
        log.info("Received payment link creation request: {}", request);
        String paymentLink = paymentService.createPaymentLink(request);
        log.info("Generated payment link: {}", paymentLink);
        return ResponseEntity.ok(paymentLink);
    }
}
