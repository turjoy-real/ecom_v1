# 3. Order Management Module

## 3.1 Order Processing

### 3.1.1 Order Creation and Management

```java
@Service
public class OrderService {

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // Validate order request
        validateOrderRequest(request);

        // Create order with items
        Order order = Order.builder()
                .userId(request.getUserId())
                .items(createOrderItems(request.getItems()))
                .totalAmount(calculateTotalAmount(request.getItems()))
                .status(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .addressId(request.getAddressId())
                .build();

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    private List<OrderItem> createOrderItems(List<OrderItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(this::createOrderItem)
                .collect(Collectors.toList());
    }

    private OrderItem createOrderItem(OrderItemRequest itemRequest) {
        // Verify product exists and has sufficient stock
        ProductDetails product = productServiceClient.getProductDetails(itemRequest.getProductId());
        if (product.getStockQuantity() < itemRequest.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + itemRequest.getProductId());
        }

        return OrderItem.builder()
                .productId(itemRequest.getProductId())
                .quantity(itemRequest.getQuantity())
                .price(product.getPrice())
                .build();
    }
}
```

---

## 3.2 Order Status Management

### 3.2.1 Order Status Flow

```java
public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED
}

public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}

@Service
public class OrderService {

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Implement status transition validation logic
        if (currentStatus == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Cannot change status of cancelled order");
        }

        if (currentStatus == OrderStatus.DELIVERED && newStatus != OrderStatus.RETURNED) {
            throw new InvalidStatusTransitionException("Delivered order can only be returned");
        }
    }
}
```

---

## 3.3 API Endpoints

### 3.3.1 Order Management Endpoints

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    // Create new order
@PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request)

    // Get order by ID
@GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId)

    // Get user's orders with filtering
    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false, defaultValue = "orderDate") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection)

    // Get user's order analytics
    @GetMapping("/my/analytics")
    public ResponseEntity<Map<String, Object>> getMyOrderAnalytics(Authentication authentication)

    // Get orders for specific user (admin only)
@GetMapping("/user/{userId}")
public ResponseEntity<List<OrderResponse>> getUserOrders(
    @PathVariable String userId,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String paymentStatus,
    @RequestParam(required = false, defaultValue = "orderDate") String sortBy,
    @RequestParam(required = false, defaultValue = "desc") String sortDirection)

    // Update order status
@PatchMapping("/{orderId}/status")
public ResponseEntity<OrderResponse> updateOrderStatus(
    @PathVariable Long orderId,
    @RequestParam String status)

    // Cancel order
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId)
}
```

### 3.3.2 Order Tracking Endpoints

```java
// Get order tracking information
@GetMapping("/{orderId}/tracking")
public ResponseEntity<OrderTrackingResponse> getOrderTracking(@PathVariable Long orderId)

// Update order tracking information
@PostMapping("/{orderId}/tracking")
public ResponseEntity<OrderTrackingResponse> updateOrderTracking(
        @PathVariable Long orderId,
        @RequestParam String trackingNumber,
        @RequestParam String carrier)
```

### 3.3.3 Return Management Endpoints

```java
// Create return request
@PostMapping("/{orderId}/return")
public ResponseEntity<ReturnRequestResponse> createReturnRequest(
        @PathVariable Long orderId,
        @RequestBody ReturnRequestDTO request)

// Get return request details
@GetMapping("/returns/{returnId}")
public ResponseEntity<ReturnRequestResponse> getReturnRequest(@PathVariable Long returnId)

// Update return status
@PatchMapping("/returns/{returnId}/status")
public ResponseEntity<ReturnRequestResponse> updateReturnStatus(
        @PathVariable Long returnId,
        @RequestParam String status)

// Get return requests by status
@GetMapping("/returns")
public ResponseEntity<List<ReturnRequestResponse>> getReturnRequestsByStatus(
        @RequestParam(required = false, defaultValue = "PENDING") String status)
```

---

## 3.4 Data Model

### 3.4.1 Order Entity

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order", orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String addressId;

    // Tracking information
    private String trackingNumber;
    private String carrier;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;

    // Helper methods
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
}

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    public double calculateTotalAmount() {
        return items.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }
}
```

### 3.4.2 Order Item Entity

```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;

    // Helper methods
    public double getSubtotal() {
        return price * quantity;
    }
}
```

### 3.4.3 Return Request Entity

```java
@Entity
@Table(name = "return_requests")
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status;

    @Column(nullable = false)
    private String reason;

    private String description;
    private LocalDateTime requestedDate;
    private LocalDateTime processedDate;
    private String adminNotes;
}
```

---

## 3.5 Business Logic

### 3.5.1 Order Validation

```java
@Service
public class OrderValidationService {

    public void validateOrderRequest(OrderRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new InvalidOrderRequestException("User ID is required");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderRequestException("Order must contain at least one item");
        }

        if (request.getAddressId() == null || request.getAddressId().trim().isEmpty()) {
            throw new InvalidOrderRequestException("Shipping address is required");
        }

        // Validate each item
        request.getItems().forEach(this::validateOrderItem);
    }

    private void validateOrderItem(OrderItemRequest item) {
        if (item.getProductId() == null) {
            throw new InvalidOrderRequestException("Product ID is required for all items");
        }

        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new InvalidOrderRequestException("Quantity must be greater than 0");
        }
    }
}
```

### 3.5.2 Order Analytics

```java
@Service
public class OrderAnalyticsService {

    public Map<String, Object> getOrderAnalytics(String userId) {
        List<Order> userOrders = orderRepository.findByUserId(userId);

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalOrders", userOrders.size());
        analytics.put("totalSpent", userOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum());
        analytics.put("averageOrderValue", userOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .average()
                .orElse(0.0));

        // Status distribution
        Map<OrderStatus, Long> statusDistribution = userOrders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        analytics.put("statusDistribution", statusDistribution);

        // Recent orders
        List<Order> recentOrders = userOrders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(5)
                .collect(Collectors.toList());
        analytics.put("recentOrders", recentOrders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList()));

        return analytics;
    }
}
```

---

## 3.6 Integration Features

### 3.6.1 Service Communication

```java
@FeignClient(name = "product-service")
public interface ProductServiceClient {
    @GetMapping("/api/products/{id}")
    ProductDetails getProductDetails(@PathVariable("id") Long productId);

    @GetMapping("/api/products/{id}/verify-stock")
    boolean verifyStock(@PathVariable("id") Long productId, @RequestParam int quantity);
}

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {
    @PostMapping("/api/payment/create-link")
    String createPaymentLink(@RequestBody CreatePaymentLinkRequestDto request);
}
```

---

## 3.7 Future Enhancements

### 3.7.1 Planned Features

- [ ] **Saga Pattern Implementation:** Distributed transaction management
- [ ] **State Machine:** Advanced order state management
- [ ] **Order Analytics Dashboard:** Comprehensive analytics and reporting
- [ ] **Automated Order Processing:** Workflow automation
- [ ] **Advanced Return Management:** Streamlined return process
- [ ] **Order Notification System:** Real-time notifications
- [ ] **Order Export Functionality:** Data export capabilities
- [ ] **Bulk Order Operations:** Batch processing capabilities

### 3.7.2 Performance Optimizations

- [ ] **Caching Layer:** Redis integration for order data
- [ ] **Database Indexing:** Optimize query performance
- [ ] **Pagination:** Efficient handling of large order datasets
- [ ] **Connection Pooling:** Optimize database connections

---

### References

- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign)
- [Spring Transaction Management](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)
