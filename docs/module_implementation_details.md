# Module Implementation Details

## 1. Product Catalog Module

### 1.1 Architecture Overview

The product catalog module follows a microservices architecture with the following components:

- REST API endpoints
- JPA for data persistence
- Redis for caching
- Elasticsearch for advanced search
- Security with OAuth2

### 1.2 Core Components

#### 1.2.1 Data Model

```java
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseModel {
    private String name;
    private String description;
    private Double price;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Category category;

    private String brand;
    private Integer stockQuantity;
    private String imageUrl;
}
```

Key concepts used:

- JPA Entity Mapping [Reference](https://docs.oracle.com/javaee/7/api/javax/persistence/Entity.html)
- Lazy Loading [Reference](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#fetching-strategies)
- Lombok Annotations [Reference](https://projectlombok.org/features/all)

#### 1.2.2 Search Implementation

```java
@GetMapping("/search")
public ResponseEntity<Page<ProductResponse>> searchProducts(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize) {
    return ResponseEntity.ok(productService.searchProductsWithPagination(keyword, pageNumber, pageSize));
}

@GetMapping("/search/advanced")
public ResponseEntity<Page<ProductResponse>> searchByMultipleCriteria(
        @RequestParam(required = false) String categoryName,
        @RequestParam(required = false) String brand,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize) {
    return ResponseEntity.ok(productService.searchByMultipleCriteria(
            categoryName, brand, minPrice, maxPrice, pageNumber, pageSize));
}
```

Search features implemented:

- Full-text search using Elasticsearch
- Multi-criteria search
- Pagination
- Fuzzy matching

#### 1.2.3 Caching Implementation

```java
@Service
public class ProductServiceImpl implements ProductService {
    private final RedisTemplate<Long, Object> redisTemplate;
    private final CacheService cacheService;

    @Override
    public ProductResponse getProductById(Long id) {
        if (cacheService.isProductCached(redisTemplate, id)) {
            return cacheService.getProductFromCache(redisTemplate, id);
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        ProductResponse response = mapToResponse(product);
        cacheService.cacheProduct(redisTemplate, id, response);
        return response;
    }
}
```

Caching concepts used:

- Redis Caching [Reference](https://redis.io/topics/caching)
- Cache-Aside Pattern [Reference](https://docs.microsoft.com/en-us/azure/architecture/patterns/cache-aside)

## 2. Cart Module

### 2.1 Architecture Overview

The cart module uses:

- MongoDB for cart storage
- Redis for caching
- Microservices communication
- Event-driven updates

### 2.2 Core Components

#### 2.2.1 Data Model

```java
@RedisHash("carts")
@Document(collection = "carts")
@AllArgsConstructor
public class Cart {
    @Id
    private String id;
    private String userId;
    private List<CartItem> items;
}

@Data
public class CartItem {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private String userId;
}
```

Key concepts:

- MongoDB Document Mapping [Reference](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mapping-usage-annotations)
- Redis Hash [Reference](https://redis.io/topics/data-types#hashes)

#### 2.2.2 Cart Operations

```java
@Service
public class CartServiceImplementation implements CartService {
    @Override
    @Transactional
    @CachePut(value = "cart", key = "#userId")
    public CartResponse addItemToCart(String userId, CartItemDTO cartItemDTO) {
        validateUser(userId);

        if (!productServiceClient.verifyStock(cartItemDTO.getProductId(), cartItemDTO.getQuantity())) {
            throw new InsufficientStockException("Insufficient stock");
        }

        CartItem existingItem = cartRepository.findByUserIdAndProductId(userId, cartItemDTO.getProductId());

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + cartItemDTO.getQuantity();
            if (!productServiceClient.verifyStock(cartItemDTO.getProductId(), newQuantity)) {
                throw new InsufficientStockException("Insufficient stock");
            }
            existingItem.setQuantity(newQuantity);
            cartRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(cartItemDTO.getProductId());
            newItem.setProductName(cartItemDTO.getProductName());
            newItem.setPrice(cartItemDTO.getPrice());
            newItem.setQuantity(cartItemDTO.getQuantity());
            newItem.setUserId(userId);
            cartRepository.save(newItem);
        }

        return getCart(userId);
    }
}
```

Key features:

- Real-time stock verification
- Atomic updates
- Cache management
- Transaction handling

## 3. Order Management Module

### 3.1 Architecture Overview

The order module implements:

- Event-driven order processing
- Saga pattern for distributed transactions
- State machine for order status
- Integration with multiple services

### 3.2 Core Components

#### 3.2.1 Data Model

```java
@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItem> items;

    private double totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime orderDate;
    private String addressId;
}
```

#### 3.2.2 Order Processing

```java
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (!userServiceClient.verifyUser(request.getUserId())) {
            throw new UserVerificationException("User not found");
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.CREATED);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setAddressId(request.getShippingAddressId());

        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> {
                    if (!productServiceClient.verifyStock(itemRequest.getProductId(), itemRequest.getQuantity())) {
                        throw new RuntimeException("Insufficient stock");
                    }

                    ProductServiceClient.ProductDetails productDetails = productServiceClient
                            .getProductDetails(itemRequest.getProductId());

                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductId(itemRequest.getProductId());
                    orderItem.setProductName(productDetails.getName());
                    orderItem.setPrice(productDetails.getPrice());
                    orderItem.setQuantity(itemRequest.getQuantity());
                    orderItem.setSubtotal(productDetails.getPrice() * itemRequest.getQuantity());
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setItems(orderItems);
        double totalAmount = orderItems.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
        order.setTotalAmount(totalAmount);

        return convertToOrderResponse(orderRepository.save(order));
    }
}
```

Key concepts:

- Saga Pattern [Reference](https://microservices.io/patterns/data/saga.html)
- Eventual Consistency [Reference](https://en.wikipedia.org/wiki/Eventual_consistency)
- Distributed Transactions [Reference](https://docs.microsoft.com/en-us/azure/architecture/patterns/saga)

## 4. Payment Module

### 4.1 Architecture Overview

The payment module implements:

- Payment gateway integration
- Webhook handling
- Payment status tracking
- Transaction management

### 4.2 Core Components

#### 4.2.1 Payment Processing

```java
@Service
@Primary
@Slf4j
public class RazorpayService implements PaymentService {
    private final RestTemplate restTemplate;
    private final String razorpayKeyId;
    private final String razorpayKeySecret;
    private static final String RAZORPAY_API_URL = "https://api.razorpay.com/v1/payment_links/";

    @Override
    public String createPaymentLink(CreatePaymentLinkRequestDto paymentRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(razorpayKeyId, razorpayKeySecret);

            JSONObject requestBody = new JSONObject();
            requestBody.put("amount", paymentRequest.getAmount() * 100); // Convert to paise
            requestBody.put("currency", "INR");
            requestBody.put("accept_partial", false);
            requestBody.put("reference_id", paymentRequest.getOrderId());
            requestBody.put("description", "Payment for order " + paymentRequest.getOrderId());
            requestBody.put("callback_url", paymentRequest.getCallbackUrl());
            requestBody.put("callback_method", "get");

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(RAZORPAY_API_URL, request, String.class);

            JSONObject responseJson = new JSONObject(response.getBody());
            return responseJson.getJSONObject("short_url").toString();
        } catch (Exception e) {
            log.error("Error creating payment link: {}", e.getMessage());
            throw new PaymentLinkCreationException("Failed to create payment link");
        }
    }
}
```

#### 4.2.2 Webhook Handling

```java
@RestController
@RequestMapping("/api/payment/webhook")
public class WebhookController {
    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader("X-Razorpay-Signature") String signature) {
        try {
            if (Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
                // Process webhook
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(payload);

                String event = jsonNode.get("event").asText();
                JsonNode payloadNode = jsonNode.get("payload");

                switch (event) {
                    case "payment.captured":
                        handlePaymentCaptured(payloadNode);
                        break;
                    case "payment.failed":
                        handlePaymentFailed(payloadNode);
                        break;
                    // Handle other events
                }

                return ResponseEntity.ok("Webhook processed successfully");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }
}
```

Key concepts:

- Payment Gateway Integration [Reference](https://razorpay.com/docs/api/)
- Webhook Security [Reference](https://razorpay.com/docs/webhooks/security/)
- Idempotency [Reference](https://stripe.com/docs/api/idempotent_requests)

### 4.3 Security Considerations

- Webhook signature verification
- API key management
- Secure payment link generation
- Transaction encryption

## Common Patterns and Best Practices

### 1. Microservices Communication

- REST APIs for synchronous communication
- Event-driven architecture for asynchronous updates
- Circuit breaker pattern for resilience
- Service discovery for dynamic routing

### 2. Data Consistency

- Eventual consistency model
- Saga pattern for distributed transactions
- Idempotent operations
- Compensation transactions

### 3. Performance Optimization

- Redis caching for frequently accessed data
- MongoDB for flexible document storage
- Elasticsearch for advanced search
- Connection pooling for database access

### 4. Security

- OAuth2 for authentication
- JWT for stateless sessions
- Role-based access control
- Input validation and sanitization

### 5. Monitoring and Logging

- Structured logging
- Performance metrics
- Error tracking
- Audit trails
