package com.services.paymentservice.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.services.paymentservice.services.RazorpayService;

@RestController
@RequestMapping("/api/razorpay/webhook")
@Slf4j
public class WebhookController {

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private final RazorpayService razorpayService;

    public WebhookController(RazorpayService razorpayService) {
        this.razorpayService = razorpayService;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestHeader("X-Razorpay-Event-Id") String eventId) {
        try {
            log.info("Received webhook event: {}", eventId);
            log.info("Payload: {}", payload);
            // Validate the webhook signature
            String result = razorpayService.handleWebhook(payload, signature, eventId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Webhook processing failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}