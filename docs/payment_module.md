# 4. Payment Module

## 4.1 Payment Gateway Integration

### 4.1.1 Razorpay Integration

```java
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
            headers.setBasicAuth(Base64.getEncoder().encodeToString(auth.getBytes()));

            // Make API call
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    RAZORPAY_API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Process response
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject responseJson = new JSONObject(response.getBody());
                return responseJson.getString("short_url");
            } else {
                throw new PaymentLinkCreationException("Failed to create payment link: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Error creating payment link: {}", e.getMessage());
            throw new PaymentLinkCreationException("Error creating payment link", e);
        }
    }
}
```

---

## 4.2 Webhook Processing

### 4.2.1 Secure Webhook Handling

```java
@RestController
@RequestMapping("/api/payment/webhook")
public class WebhookController {
    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        // 1. Verify signature
        if (!verifySignature(payload, signature)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid signature");
        }

        try {
            // 2. Parse payload
            WebhookEvent event = parseWebhookPayload(payload);

            // 3. Process event
            processWebhookEvent(event);

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook");
        }
    }

    private boolean verifySignature(String payload, String signature) {
        try {
            return Utils.verifyWebhookSignature(payload, signature, webhookSecret);
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    private void processWebhookEvent(WebhookEvent event) {
        switch (event.getType()) {
            case "payment.captured":
                handlePaymentCaptured(event);
                break;
            case "payment.failed":
                handlePaymentFailed(event);
                break;
            case "refund.processed":
                handleRefundProcessed(event);
                break;
            default:
                log.warn("Unhandled webhook event type: {}", event.getType());
        }
    }
}
```

---

### References

- [Razorpay API Documentation](https://razorpay.com/docs/api/)
- [Spring Security](https://spring.io/projects/spring-security)
- [Spring Web](https://spring.io/projects/spring-web)
