package com.services.paymentservice.controllers;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@RestController
@RequestMapping("/api/payment/webhook")
public class WebhookController {

    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;

    @Autowired
    @Qualifier("loadBalancedRestTemplate")
    private RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        log.info("Received webhook request with signature: {}", signature);
        log.debug("Webhook payload: {}", payload);

        try {
            if (payload == null || signature == null) {
                log.error("Missing payload or signature in webhook request");
                return ResponseEntity.badRequest().body("Missing payload or signature");
            }

            if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
                log.error("Webhook secret is not configured");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Webhook secret not configured");
            }

            // Verify webhook signature
            log.info("Verifying webhook signature");
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!isValid) {
                log.error("Invalid webhook signature. Expected signature: {}, Received: {}", signature, webhookSecret);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }
            log.info("Webhook signature verified successfully");

            // Parse the webhook payload
            log.info("Parsing webhook payload");
            JsonNode jsonNode = new ObjectMapper().readTree(payload);
            if (!jsonNode.has("event") || !jsonNode.has("payload")) {
                log.error("Invalid webhook payload format. Missing required fields: event or payload");
                return ResponseEntity.badRequest().body("Invalid webhook payload format");
            }

            String event = jsonNode.get("event").asText();
            log.info("Processing webhook event: {}", event);

            JsonNode paymentNode = jsonNode.get("payload").get("payment");
            if (paymentNode == null || !paymentNode.has("entity")) {
                log.error("Invalid payment data in webhook. Payment node or entity missing");
                return ResponseEntity.badRequest().body("Invalid payment data in webhook");
            }

            JsonNode payment = paymentNode.get("entity");
            if (!payment.has("notes") || !payment.get("notes").has("order_id")) {
                log.error("Missing order_id in payment notes. Payment data: {}", payment.toString());
                return ResponseEntity.badRequest().body("Missing order_id in payment notes");
            }

            // Extract order ID from payment notes
            String orderId = payment.get("notes").get("order_id").asText();
            log.info("Extracted order ID from webhook: {}", orderId);

            // Extract payment ID for logging
            String paymentId = payment.has("id") ? payment.get("id").asText() : "unknown";
            log.info("Processing payment ID: {} for order: {}", paymentId, orderId);

            String paymentStatus;
            // Map Razorpay event to payment status
            switch (event) {
                case "payment.captured":
                    paymentStatus = "PAID";
                    log.info("Payment captured for order: {} with payment ID: {}", orderId, paymentId);
                    break;
                case "payment.failed":
                    paymentStatus = "FAILED";
                    String failureReason = payment.has("error_code")
                            ? payment.get("error_code").asText() + ": " + payment.get("error_description").asText()
                            : "Unknown reason";
                    log.info("Payment failed for order: {} with payment ID: {}. Reason: {}",
                            orderId, paymentId, failureReason);
                    break;
                case "refund.processed":
                    paymentStatus = "REFUNDED";
                    String refundId = payment.has("refund_id") ? payment.get("refund_id").asText() : "unknown";
                    log.info("Refund processed for order: {} with payment ID: {} and refund ID: {}",
                            orderId, paymentId, refundId);
                    break;
                default:
                    log.info("Unhandled webhook event: {} for order: {} with payment ID: {}",
                            event, orderId, paymentId);
                    return ResponseEntity.ok("Event ignored");
            }

            try {
                // Call order service to update payment status
                String orderServiceUrl = "http://orderservice/api/orders/" + orderId + "/payment-status?paymentStatus="
                        + paymentStatus;
                log.info("Calling order service to update payment status. URL: {}", orderServiceUrl);

                restTemplate.patchForObject(orderServiceUrl, null, Void.class);

                log.info("Successfully updated payment status for order {} to {} for payment ID: {}",
                        orderId, paymentStatus, paymentId);
                return ResponseEntity.ok("Webhook processed successfully");
            } catch (Exception e) {
                log.error("Error calling order service for order {} with payment ID {}: {}",
                        orderId, paymentId, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error updating order status: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }
}