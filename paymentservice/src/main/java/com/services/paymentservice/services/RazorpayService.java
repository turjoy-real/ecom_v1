package com.services.paymentservice.services;

import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.Webhook;
import com.services.paymentservice.dtos.CreatePaymentLinkRequestDto;
import com.services.paymentservice.exceptions.PaymentLinkCreationException;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Base64;
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

    // @Override
    // public String createPaymentLink(CreatePaymentLinkRequestDto paymentRequest) {
    // try {
    // log.info("Creating payment link for order: {}", paymentRequest.getOrderId());

    // // Build request body
    // JSONObject requestBody = new JSONObject();
    // requestBody.put("amount", (int) (paymentRequest.getAmount() * 100)); //
    // amount in paise
    // requestBody.put("currency", paymentRequest.getCurrency());
    // requestBody.put("accept_partial", paymentRequest.isAcceptPartial());
    // requestBody.put("first_min_partial_amount", (int)
    // (paymentRequest.getFirstMinPartialAmount() * 100));
    // requestBody.put("reference_id", paymentRequest.getOrderId());
    // requestBody.put("description", paymentRequest.getDescription());

    // JSONObject customer = new JSONObject();
    // customer.put("name", paymentRequest.getCustomerName());
    // customer.put("email", paymentRequest.getCustomerEmail());
    // customer.put("contact", paymentRequest.getCustomerContact());
    // requestBody.put("customer", customer);

    // JSONObject notify = new JSONObject();
    // notify.put("sms", true);
    // notify.put("email", true);
    // requestBody.put("notify", notify);

    // requestBody.put("reminder_enable", paymentRequest.isReminderEnable());

    // JSONObject options = new JSONObject();
    // JSONObject checkout = new JSONObject();
    // JSONObject theme = new JSONObject();
    // theme.put("hide_topbar", true);
    // checkout.put("theme", theme);
    // options.put("checkout", checkout);
    // requestBody.put("options", options);

    // // Setup headers
    // HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_JSON);
    // String auth = razorpayKeyId + ":" + razorpayKeySecret;
    // String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
    // headers.set("Authorization", "Basic " + encodedAuth);

    // // Make request
    // HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(),
    // headers);
    // ResponseEntity<String> rawResponse = restTemplate.exchange(
    // RAZORPAY_API_URL,
    // HttpMethod.POST,
    // entity,
    // String.class);

    // log.info("Raw Razorpay response: {}", rawResponse.getBody());

    // if (rawResponse.getStatusCode() == HttpStatus.OK && rawResponse.getBody() !=
    // null) {
    // JSONObject jsonObject = new JSONObject(rawResponse.getBody());
    // String shortUrl = jsonObject.getString("short_url");
    // log.info("Successfully created Razorpay payment link: {}", shortUrl);
    // return shortUrl;
    // } else {
    // log.error("Failed to create payment link: {}", rawResponse);
    // throw new PaymentLinkCreationException("Razorpay API did not return expected
    // response");
    // }

    // } catch (Exception e) {
    // log.error("Error creating payment link for order {}: {}",
    // paymentRequest.getOrderId(), e.getMessage(), e);
    // throw new PaymentLinkCreationException("Error creating payment link: " +
    // e.getMessage(), e);
    // }
    // }

}