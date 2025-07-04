package com.services.paymentservice.services;

import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;

import com.services.common.dtos.CreatePaymentLinkRequestDto;
import com.razorpay.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.services.common.enums.PaymentStatus;
import com.services.paymentservice.clients.OrderServiceFeignClient;

import com.services.paymentservice.models.Payment;
import com.services.paymentservice.repositories.PaymentRepository;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import com.services.common.dtos.OrderResponse;

@Service
@Primary
@Slf4j
@RequiredArgsConstructor
public class RazorpayService implements PaymentService {

    // private final RestTemplate restTemplate;
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.webhook.secret}")
    private String razorpayWebsecret;

    private final RazorpayClient razorpayClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PaymentRepository paymentRepository;

    @Autowired
    private OrderServiceFeignClient orderServiceFeignClient;
    @Autowired
    private OAuthClientCredentialsService oAuthClientCredentialsService;

    public String createPaymentLink(CreatePaymentLinkRequestDto reqDto) {
        try {
            log.info("Creating payment link for order {}", reqDto.getOrderId());

            JSONObject payload = new JSONObject()
                    .put("amount", (int) (reqDto.getAmount() * 100))
                    .put("currency", reqDto.getCurrency())
                    .put("accept_partial", reqDto.isAcceptPartial());

            if (reqDto.isAcceptPartial()) {
                payload.put("first_min_partial_amount", (int) (reqDto.getFirstMinPartialAmount() * 100));
            }

            payload.put("reference_id", reqDto.getOrderId())
                    .put("description", reqDto.getDescription())
                    .put("reminder_enable", reqDto.isReminderEnable());

            JSONObject customer = new JSONObject()
                    .put("name", reqDto.getCustomerName())
                    .put("email", reqDto.getCustomerEmail())
                    .put("contact", reqDto.getCustomerContact());

            payload.put("customer", customer)
                    .put("notify", new JSONObject().put("sms", true).put("email", true));

            // Optional: hide topbar
            JSONObject options = new JSONObject()
                    .put("checkout", new JSONObject()
                            .put("theme", new JSONObject().put("hide_topbar", true)));
            payload.put("options", options);

            PaymentLink link = razorpayClient.paymentLink.create(payload);

            log.info("Payload for Razorpay SDK: {}", link.toString());
            String shortUrl = link.get("short_url");

            log.info("Created payment link: {}", shortUrl);
            return shortUrl;

        } catch (Exception e) {
            log.error("Failed to create payment link for order {} : {}",
                    reqDto.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("Payment link creation failed", e);
        }
    }

    public String handleWebhook(String payload, String signature, String eventId) {
        log.info("Raw webhook payload: {}", payload);
        log.info("Received Razorpay webhook: eventId={}, signature={}", eventId, signature);
        // 1. Validate payload and signature
        try {
            Utils.verifyWebhookSignature(payload, signature, razorpayWebsecret);
        } catch (Exception e) {
            log.error("Invalid Razorpay signature for event {}: {}", eventId, e.getMessage());
            throw new RuntimeException("Invalid signature");
        }
        log.info("✅ Webhook signature validated (eventId={})", eventId);
        // 2. Parse JSON and event type
        JsonNode json;
        try {
            json = new ObjectMapper().readTree(payload);
            log.info("Successfully parsed webhook payload. Event ID: {}, Payload: {}", eventId, json.toPrettyString());
        } catch (Exception e) {
            log.error("Invalid JSON payload: {}", e.getMessage());
            throw new RuntimeException("Invalid JSON");
        }
        String event = json.path("event").asText(null);
        if (event == null) {
            log.warn("Ignoring webhook: missing 'event' field");
            throw new RuntimeException("Missing event");
        }
        log.info("Processing Razorpay event: {}", event);
        // 3. Handle events
        JsonNode payloadEntity = json.path("payload").path("payment").path("entity");
        switch (event) {
            case "payment_link.expired":
                log.warn("⏰ Payment link expired: id={}, amount={}",
                        payloadEntity.path("id").asText(),
                        payloadEntity.path("amount").asInt());
                // Call orderservice to update payment status
                try {
                    String orderId = payloadEntity.path("reference_id").asText();
                    String token = oAuthClientCredentialsService.getToken();
                    orderServiceFeignClient.updatePaymentStatus(token, Long.valueOf(orderId), PaymentStatus.FAILED);
                    log.info("OrderService payment status set to FAILED for expired payment link, orderId={}", orderId);
                } catch (Exception ex) {
                    log.error("Failed to update payment status in orderservice", ex);
                }
                break;
            case "payment_link.cancelled":
                log.warn("🚫 Payment link cancelled: id={}, error={}",
                        payloadEntity.path("id").asText(),
                        payloadEntity.path("error_code").asText("UNKNOWN"));
                // Optionally update order as failed
                try {
                    String orderId = payloadEntity.path("reference_id").asText();
                    String token = oAuthClientCredentialsService.getToken();
                    orderServiceFeignClient.updatePaymentStatus(token, Long.valueOf(orderId), PaymentStatus.FAILED);
                    log.info("OrderService payment status set to FAILED for cancelled payment link, orderId={}",
                            orderId);
                } catch (Exception ex) {
                    log.error("Failed to update payment status in orderservice", ex);
                }
                break;
            case "payment_link.paid":
                JsonNode paymentLinkEntity = json.path("payload").path("payment_link").path("entity");
                String orderIdStr = paymentLinkEntity.path("reference_id").asText();
                if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
                    log.error("Order ID (reference_id) is missing in payment_link.paid event payload: {}",
                            paymentLinkEntity);
                    break;
                }
                Long orderId = Long.valueOf(orderIdStr);
                String recipientEmail = paymentLinkEntity.path("customer").path("email").asText();
                int amount = paymentLinkEntity.path("amount_paid").asInt();

                log.info("💰 Payment link fully paid: orderId={}, email={}, amount={}", orderId, recipientEmail,
                        amount);

                try {
                    String token = oAuthClientCredentialsService.getToken();

                    // 1. Update OrderStatus to PLACED
                    orderServiceFeignClient.updateOrderStatus(token, orderId, "PLACED");
                    log.info("OrderService order status set to PLACED for orderId={}", orderId);

                    // 2. Update PaymentStatus to COMPLETED
                    orderServiceFeignClient.updatePaymentStatus(token, orderId, PaymentStatus.COMPLETED);
                    log.info("OrderService payment status set to COMPLETED for orderId={}", orderId);

                    // 3. Save payment record
                    JsonNode paymentEntity = json.path("payload").path("payment").path("entity");
                    Payment payment = new Payment();
                    payment.setOrderId(orderIdStr);
                    payment.setUserId(""); // Set if available
                    payment.setAmount(amount / 100.0); // Razorpay sends amount in paise
                    payment.setCurrency(paymentEntity.path("currency").asText("INR"));
                    payment.setPaymentMethod(paymentEntity.path("method").asText(""));
                    payment.setTransactionId(paymentEntity.path("id").asText(""));
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setCreatedAt(LocalDateTime.now());
                    payment.setUpdatedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                    log.info("Payment record saved for orderId={}", orderId);

                    // 4. Fetch order details for invoice
                    String orderDetails;
                    try {
                        OrderResponse orderResponse = orderServiceFeignClient.getOrderById(token, orderId);
                        if (orderResponse != null && orderResponse.getItems() != null) {
                            StringBuilder detailsBuilder = new StringBuilder();
                            for (OrderResponse.OrderItemResponse item : orderResponse.getItems()) {
                                detailsBuilder.append(item.getProductName())
                                        .append(" x ")
                                        .append(item.getQuantity())
                                        .append(", ");
                            }
                            orderDetails = detailsBuilder.toString();
                            if (orderDetails.endsWith(", ")) {
                                orderDetails = orderDetails.substring(0, orderDetails.length() - 2);
                            }
                        } else {
                            orderDetails = "Order details unavailable";
                        }
                    } catch (Exception ex) {
                        log.error("Failed to fetch order details for invoice: {}", orderId, ex);
                        orderDetails = "Order details unavailable";
                    }

                    // 5. Send payment invoice to user via Kafka
                    String invoiceMsg = orderId + " // " + recipientEmail + " // " + "INR " + amount / 100 + " // "
                            + orderDetails;
                    kafkaTemplate.send("payment-invoice", invoiceMsg);
                    log.info(
                            "Payment invoice sent to notification service for orderId={}, email={}, amount={}, details={}",
                            orderId, recipientEmail, amount, orderDetails);

                } catch (Exception ex) {
                    log.error("Failed to process payment_link.paid event in orderservice/notificationservice", ex);
                }
                break;
            case "payment_link.partially_paid":
                log.info("💸 Payment link partially paid: id={}, amount={}",
                        payloadEntity.path("id").asText(),
                        payloadEntity.path("amount").asInt());
                break;
            default:
                log.info("ℹ️ Unhandled event: {}", event);
                return "Event ignored";
        }
        return "Webhook processed";
    }
}