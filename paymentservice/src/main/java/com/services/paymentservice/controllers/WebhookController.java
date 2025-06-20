package com.services.paymentservice.controllers;

import com.razorpay.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/razorpay/webhook")
@Slf4j
public class WebhookController {

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestHeader("X-Razorpay-Event-Id") String eventId) {

        log.debug("Received Razorpay webhook: eventId={}, signature={}", eventId, signature);

        // 1. Validate payload and signature
        try {
            Utils.verifyWebhookSignature(payload, signature, webhookSecret);
        } catch (Exception e) {
            log.error("Invalid Razorpay signature for event {}: {}", eventId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }
        log.info("✅ Webhook signature validated (eventId={})", eventId);

        // 2. Parse JSON and event type
        JsonNode json;
        try {
            json = new ObjectMapper().readTree(payload);
            log.info("Successfully parsed webhook payload. Event ID: {}, Payload: {}", eventId, json.toPrettyString());
        } catch (Exception e) {
            log.error("Invalid JSON payload: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid JSON");
        }
        String event = json.path("event").asText(null);
        if (event == null) {
            log.warn("Ignoring webhook: missing 'event' field");
            return ResponseEntity.badRequest().body("Missing event");
        }
        log.info("Processing Razorpay event: {}", event);

        // 3. Ensure idempotency using event identifier
        // (Check if eventId already processed in storage; pseudo-code)
        // if (eventStore.exists(eventId)) { return ResponseEntity.ok("Event ignored");
        // }
        // eventStore.record(eventId);

        // 4. Handle events
        JsonNode payloadEntity = json.path("payload").path("payment").path("entity");
        switch (event) {
            case "payment_link.captured":
                log.info("✅ Payment captured: id={}, amount={}",
                        payloadEntity.path("id").asText(),
                        payloadEntity.path("amount").asInt());
                break;
            case "payment_link.failed":
                log.warn("❌ Payment failed: id={}, error={}",
                        payloadEntity.path("id").asText(),
                        payloadEntity.path("error_code").asText("UNKNOWN"));
                break;
            case "payment_link.paid":
                log.info("🔗 Payment syccessful: id={}, short_url={}",
                        payloadEntity.path("id").asText(),
                        payloadEntity.path("amount").asInt());
                break;
            default:
                log.info("ℹ️ Unhandled event: {}", event);
                return ResponseEntity.ok("Event ignored");
        }

        // 5. Business logic (e.g., update DB, order status)
        // orderService.updateStatus(...);

        return ResponseEntity.ok("Webhook processed");
    }
}