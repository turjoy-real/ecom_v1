package com.services.paymentservice.services;

import com.services.paymentservice.dtos.CreatePaymentLinkRequestDto;
import com.services.paymentservice.exceptions.PaymentLinkCreationException;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
@Primary
@Slf4j
public class RazorpayService implements PaymentService {

    private final RestTemplate restTemplate;
    private final String razorpayKeyId;
    private final String razorpayKeySecret;
    private static final String RAZORPAY_API_URL = "https://api.razorpay.com/v1/payment_links/";

    public RazorpayService(
            @Qualifier("externalRestTemplate") RestTemplate restTemplate,
            @Value("${razorpay.key.id}") String razorpayKeyId,
            @Value("${razorpay.key.secret}") String razorpayKeySecret) {
        this.restTemplate = restTemplate;
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
        log.info("RazorpayService initialized with keyId: {}", razorpayKeyId);
    }

    @Override
    public String createPaymentLink(CreatePaymentLinkRequestDto paymentRequest) {
        try {
            log.info("Creating payment link for order: {}", paymentRequest.getOrderId());

            // Build request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("amount", (int) (paymentRequest.getAmount() * 100)); // amount in paise
            requestBody.put("currency", paymentRequest.getCurrency());
            requestBody.put("accept_partial", paymentRequest.isAcceptPartial());
            requestBody.put("first_min_partial_amount", (int) (paymentRequest.getFirstMinPartialAmount() * 100));
            requestBody.put("reference_id", paymentRequest.getOrderId());
            requestBody.put("description", paymentRequest.getDescription());

            JSONObject customer = new JSONObject();
            customer.put("name", paymentRequest.getCustomerName());
            customer.put("email", paymentRequest.getCustomerEmail());
            customer.put("contact", paymentRequest.getCustomerContact());
            requestBody.put("customer", customer);

            JSONObject notify = new JSONObject();
            notify.put("sms", true);
            notify.put("email", true);
            requestBody.put("notify", notify);

            requestBody.put("reminder_enable", paymentRequest.isReminderEnable());

            JSONObject options = new JSONObject();
            JSONObject checkout = new JSONObject();
            JSONObject theme = new JSONObject();
            theme.put("hide_topbar", true);
            checkout.put("theme", theme);
            options.put("checkout", checkout);
            requestBody.put("options", options);

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String auth = razorpayKeyId + ":" + razorpayKeySecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            // Make request
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    RAZORPAY_API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class);

            log.info("Raw Razorpay response: {}", rawResponse.getBody());

            if (rawResponse.getStatusCode() == HttpStatus.OK && rawResponse.getBody() != null) {
                JSONObject jsonObject = new JSONObject(rawResponse.getBody());
                String shortUrl = jsonObject.getString("short_url");
                log.info("Successfully created Razorpay payment link: {}", shortUrl);
                return shortUrl;
            } else {
                log.error("Failed to create payment link: {}", rawResponse);
                throw new PaymentLinkCreationException("Razorpay API did not return expected response");
            }

        } catch (Exception e) {
            log.error("Error creating payment link for order {}: {}", paymentRequest.getOrderId(), e.getMessage(), e);
            throw new PaymentLinkCreationException("Error creating payment link: " + e.getMessage(), e);
        }
    }
}