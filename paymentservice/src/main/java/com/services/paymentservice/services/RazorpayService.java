package com.services.paymentservice.services;

import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.Webhook;
import com.services.common.dtos.CreatePaymentLinkRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;

import org.springframework.stereotype.Service;

import java.util.Arrays;

import java.util.List;

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

    public Webhook createWebhook(String accountId,
            String callbackUrl,
            String alertEmail) {
        try {
            JSONObject webhookRequest = new JSONObject();
            webhookRequest.put("url", callbackUrl);
            webhookRequest.put("alert_email", alertEmail);
            webhookRequest.put("secret", razorpayWebsecret);

            List<String> events = Arrays.asList(
                    "payment.authorized",
                    "payment.failed",
                    "payment.captured",
                    "payment.dispute.created",
                    "refund.failed",
                    "refund.created");
            webhookRequest.put("events", events);

            log.info("Creating webhook for account: {}", accountId);
            return razorpayClient.webhook.create(accountId, webhookRequest);

        } catch (Exception e) {
            log.error("Error creating webhook for account {}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("Failed to create webhook", e);
        }
    }

}