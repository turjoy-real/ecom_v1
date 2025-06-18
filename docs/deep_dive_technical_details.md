# Deep Dive Technical Documentation

## 1. Product Catalog Module

### 1.1 Data Layer

#### 1.1.1 Entity Design

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

Key Design Decisions:

1. **Lazy Loading for Category**

   - Reduces initial query load
   - Improves performance for product listings
   - Category details loaded only when needed
   - Prevents N+1 query problems

2. **Cascade Operations**

   - Category deletion cascades to products
   - Ensures data consistency
   - Prevents orphaned records

3. **BaseModel Extension**
   - Common fields (id, timestamps)
   - Audit trail support
   - Consistent entity structure

#### 1.1.2 Repository Layer

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(Category category, Pageable pageable);
    List<Product> findByBrand(String brand);
    List<Product> findByNameContainingIgnoreCase(String keyword);
}
```

Advanced Query Features:

1. **Pagination Support**

   - Efficient handling of large datasets
   - Memory optimization
   - Better user experience

2. **Dynamic Queries**
   - Flexible search capabilities
   - Case-insensitive matching
   - Partial text matching

### 1.2 Search Implementation

#### 1.2.1 Elasticsearch Integration

```java
@Document(indexName = "products")
public class ProductDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Integer)
    private Integer stockQuantity;
}
```

Search Features:

1. **Full-Text Search**

   - Standard analyzer for text fields
   - Keyword fields for exact matching
   - Numeric fields for range queries

2. **Advanced Queries**

```java
public Page<ProductResponse> searchByMultipleCriteria(
        String categoryName,
        String brand,
        Double minPrice,
        Double maxPrice,
        int pageNumber,
        int pageSize) {

    BoolQueryBuilder query = QueryBuilders.boolQuery();

    if (categoryName != null) {
        query.must(QueryBuilders.matchQuery("category.name", categoryName));
    }

    if (brand != null) {
        query.must(QueryBuilders.termQuery("brand", brand));
    }

    if (minPrice != null || maxPrice != null) {
        RangeQueryBuilder priceRange = QueryBuilders.rangeQuery("price");
        if (minPrice != null) priceRange.gte(minPrice);
        if (maxPrice != null) priceRange.lte(maxPrice);
        query.must(priceRange);
    }

    return productElasticsearchRepository.search(query, PageRequest.of(pageNumber, pageSize));
}
```

### 1.3 Caching Strategy

#### 1.3.1 Redis Implementation

```java
@Service
public class CacheService {
    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final long CACHE_TTL = 3600; // 1 hour

    public void cacheProduct(RedisTemplate<Long, Object> redisTemplate, Long id, ProductResponse product) {
        String key = PRODUCT_CACHE_PREFIX + id;
        redisTemplate.opsForValue().set(key, product, CACHE_TTL, TimeUnit.SECONDS);
    }

    public ProductResponse getProductFromCache(RedisTemplate<Long, Object> redisTemplate, Long id) {
        String key = PRODUCT_CACHE_PREFIX + id;
        return (ProductResponse) redisTemplate.opsForValue().get(key);
    }
}
```

Caching Features:

1. **TTL Management**

   - Configurable cache duration
   - Automatic cache invalidation
   - Memory optimization

2. **Cache Patterns**
   - Cache-Aside pattern
   - Write-Through pattern for updates
   - Cache invalidation on updates

## 2. Cart Module

### 2.1 Data Storage

#### 2.1.1 MongoDB Schema

```java
@Document(collection = "carts")
@RedisHash("carts")
public class Cart {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Version
    private Long version;

    private List<CartItem> items;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

Design Considerations:

1. **Dual Storage Strategy**

   - MongoDB for persistence
   - Redis for performance
   - Eventual consistency model

2. **Optimistic Locking**
   - Version field for concurrency
   - Prevents race conditions
   - Handles concurrent updates

#### 2.1.2 Cart Operations

```java
@Service
public class CartServiceImplementation implements CartService {
    @Override
    @Transactional
    @CachePut(value = "cart", key = "#userId")
    public CartResponse addItemToCart(String userId, CartItemDTO cartItemDTO) {
        // Stock verification
        if (!productServiceClient.verifyStock(cartItemDTO.getProductId(), cartItemDTO.getQuantity())) {
            throw new InsufficientStockException("Insufficient stock");
        }

        // Atomic update
        CartItem existingItem = cartRepository.findByUserIdAndProductId(userId, cartItemDTO.getProductId());
        if (existingItem != null) {
            return updateExistingItem(existingItem, cartItemDTO);
        } else {
            return addNewItem(userId, cartItemDTO);
        }
    }

    private CartResponse updateExistingItem(CartItem existingItem, CartItemDTO newItem) {
        int newQuantity = existingItem.getQuantity() + newItem.getQuantity();
        if (!productServiceClient.verifyStock(newItem.getProductId(), newQuantity)) {
            throw new InsufficientStockException("Insufficient stock for updated quantity");
        }

        existingItem.setQuantity(newQuantity);
        cartRepository.save(existingItem);
        return getCart(existingItem.getUserId());
    }
}
```

### 2.2 Integration Patterns

#### 2.2.1 Service Communication

```java
@FeignClient(name = "product-service")
public interface ProductServiceClient {
    @GetMapping("/api/products/{id}/verify-stock")
    boolean verifyStock(@PathVariable("id") Long productId, @RequestParam int quantity);

    @GetMapping("/api/products/{id}")
    ProductDetails getProductDetails(@PathVariable("id") Long productId);
}
```

Integration Features:

1. **Circuit Breaker Pattern**

   - Resilience against service failures
   - Fallback mechanisms
   - Timeout handling

2. **Load Balancing**
   - Service discovery integration
   - Dynamic routing
   - Health checks

## 3. Order Management Module

### 3.1 Transaction Management

#### 3.1.1 Saga Pattern Implementation

```java
@Service
public class OrderSagaManager {
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;

    @Transactional
    public void processOrder(OrderRequest request) {
        // 1. Create Order
        Order order = orderService.createOrder(request);

        try {
            // 2. Reserve Inventory
            inventoryService.reserveItems(order.getItems());

            // 3. Process Payment
            PaymentResult payment = paymentService.processPayment(order);

            if (payment.isSuccessful()) {
                // 4. Confirm Order
                orderService.confirmOrder(order.getId());
            } else {
                // 5. Compensate
                compensateOrder(order);
            }
        } catch (Exception e) {
            // 6. Compensate on failure
            compensateOrder(order);
            throw e;
        }
    }

    private void compensateOrder(Order order) {
        try {
            inventoryService.releaseItems(order.getItems());
            paymentService.refundPayment(order.getPaymentId());
            orderService.cancelOrder(order.getId());
        } catch (Exception e) {
            // Log compensation failure
            log.error("Compensation failed for order: {}", order.getId(), e);
        }
    }
}
```

### 3.2 State Management

#### 3.2.1 Order State Machine

```java
@Configuration
@EnableStateMachineFactory
public class OrderStateMachineConfig extends StateMachineConfigurerAdapter<OrderStatus, OrderEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEvent> states) throws Exception {
        states
            .withStates()
            .initial(OrderStatus.CREATED)
            .state(OrderStatus.PAYMENT_PENDING)
            .state(OrderStatus.PAYMENT_COMPLETED)
            .state(OrderStatus.INVENTORY_RESERVED)
            .state(OrderStatus.SHIPPED)
            .state(OrderStatus.DELIVERED)
            .state(OrderStatus.CANCELLED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEvent> transitions) throws Exception {
        transitions
            .withExternal()
                .source(OrderStatus.CREATED)
                .target(OrderStatus.PAYMENT_PENDING)
                .event(OrderEvent.PAYMENT_INITIATED)
            .and()
            .withExternal()
                .source(OrderStatus.PAYMENT_PENDING)
                .target(OrderStatus.PAYMENT_COMPLETED)
                .event(OrderEvent.PAYMENT_COMPLETED)
            // ... more transitions
    }
}
```

## 4. Payment Module

### 4.1 Payment Gateway Integration

#### 4.1.1 Razorpay Integration

```java
@Service
@Primary
@Slf4j
public class RazorpayService implements PaymentService {
    private final RestTemplate restTemplate;
    private final String razorpayKeyId;
    private final String razorpayKeySecret;

    @Override
    public String createPaymentLink(CreatePaymentLinkRequestDto request) {
        try {
            // 1. Prepare headers
            HttpHeaders headers = createSecureHeaders();

            // 2. Prepare request body
            JSONObject requestBody = createPaymentRequestBody(request);

            // 3. Make API call
            ResponseEntity<String> response = makeApiCall(headers, requestBody);

            // 4. Process response
            return processPaymentResponse(response);
        } catch (Exception e) {
            handlePaymentError(e);
        }
    }

    private HttpHeaders createSecureHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(razorpayKeyId, razorpayKeySecret);
        return headers;
    }

    private JSONObject createPaymentRequestBody(CreatePaymentLinkRequestDto request) {
        JSONObject body = new JSONObject();
        body.put("amount", request.getAmount() * 100); // Convert to paise
        body.put("currency", "INR");
        body.put("accept_partial", false);
        body.put("reference_id", request.getOrderId());
        body.put("description", "Payment for order " + request.getOrderId());
        body.put("callback_url", request.getCallbackUrl());
        body.put("callback_method", "get");
        return body;
    }
}
```

### 4.2 Webhook Processing

#### 4.2.1 Secure Webhook Handling

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

## 5. Security Implementation

### 5.1 Authentication & Authorization

#### 5.1.1 OAuth2 Configuration

```java
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(JwtDecoders.fromIssuerLocation(issuerUri))
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .cors(withDefaults())
            .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                context.getClaims().claims((claims) -> {
                    Set<String> roles = AuthorityUtils.authorityListToSet(
                            context.getPrincipal().getAuthorities())
                            .stream()
                            .map(c -> c.replaceFirst("^ROLE_", ""))
                            .collect(Collectors.collectingAndThen(
                                    Collectors.toSet(),
                                    Collections::unmodifiableSet));
                    claims.put("roles", roles);
                });
            }
        };
    }
}
```

### 5.2 Data Security

#### 5.2.1 Encryption & Hashing

```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public EncryptionService encryptionService(
            @Value("${encryption.key}") String encryptionKey) {
        return new AESEncryptionService(encryptionKey);
    }
}

@Service
public class AESEncryptionService implements EncryptionService {
    private final SecretKey key;
    private final Cipher cipher;

    public AESEncryptionService(String encryptionKey) {
        this.key = new SecretKeySpec(
            Base64.getDecoder().decode(encryptionKey),
            "AES"
        );
        this.cipher = Cipher.getInstance("AES/GCM/NoPadding");
    }

    @Override
    public String encrypt(String data) {
        try {
            byte[] iv = generateIV();
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(
                ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array()
            );
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }
}
```

## 6. Performance Optimization

### 6.1 Caching Strategies

#### 6.1.1 Multi-Level Caching

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<Cache> caches = new ArrayList<>();

        // Redis cache for products
        caches.add(new RedisCache(
            "products",
            redisTemplate,
            Duration.ofMinutes(30)
        ));

        // Caffeine cache for categories
        caches.add(new CaffeineCache(
            "categories",
            Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build()
        ));

        cacheManager.setCaches(caches);
        return cacheManager;
    }
}
```

### 6.2 Database Optimization

#### 6.2.1 Indexing Strategy

```sql
-- Product table indexes
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_brand ON products(brand);
CREATE INDEX idx_product_price ON products(price);
CREATE INDEX idx_product_stock ON products(stock_quantity);

-- Order table indexes
CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_payment_status ON orders(payment_status);
CREATE INDEX idx_order_date ON orders(order_date);
```

## 7. Monitoring and Logging

### 7.1 Logging Implementation

#### 7.1.1 Structured Logging

```java
@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        log.info("Method: {} executed in {} ms",
            joinPoint.getSignature().getName(),
            endTime - startTime);

        return result;
    }
}

@Aspect
@Component
@Slf4j
public class ExceptionLoggingAspect {
    @AfterThrowing(
        pointcut = "execution(* com.services..*.*(..))",
        throwing = "ex"
    )
    public void logException(JoinPoint joinPoint, Exception ex) {
        log.error("Exception in {}.{}() with cause = {}",
            joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName(),
            ex.getCause() != null ? ex.getCause() : "NULL");
    }
}
```

### 7.2 Metrics Collection

#### 7.2.1 Performance Metrics

```java
@Configuration
public class MetricsConfig {
    @Bean
    public MeterRegistry meterRegistry() {
        CompositeMeterRegistry registry = new CompositeMeterRegistry();
        registry.add(new SimpleMeterRegistry());
        registry.add(new JmxMeterRegistry(JmxConfig.DEFAULT, Clock.SYSTEM));
        return registry;
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}

@Service
public class ProductService {
    @Timed(value = "product.search.time", description = "Time taken to search products")
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        // Implementation
    }

    @Counted(value = "product.creation.count", description = "Number of products created")
    public ProductResponse createProduct(ProductRequest request) {
        // Implementation
    }
}
```
