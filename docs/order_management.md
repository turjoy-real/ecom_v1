# 3. Order Management Module

## 3.1 Transaction Management

### 3.1.1 Saga Pattern Implementation

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

---

## 3.2 State Management

### 3.2.1 Order State Machine

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

---

### References

- [Spring State Machine](https://spring.io/projects/spring-statemachine)
- [Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

## Implemented Features

### 1. Order Creation

```java
@PostMapping
public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
    return ResponseEntity.ok(orderService.createOrder(request));
}
```

- Creates new order with items
- Verifies product stock availability
- Calculates total amount
- Associates shipping address
- Sets initial order status

### 2. Order Retrieval

```java
@GetMapping("/{orderId}")
public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
    return ResponseEntity.ok(orderService.getOrder(orderId));
}
```

- Retrieves order by ID
- Includes order items, status, and payment details

### 3. User Orders

```java
@GetMapping("/user/{userId}")
public ResponseEntity<List<OrderResponse>> getUserOrders(
    @PathVariable String userId,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String paymentStatus,
    @RequestParam(required = false, defaultValue = "orderDate") String sortBy,
    @RequestParam(required = false, defaultValue = "desc") String sortDirection)
```

- Lists all orders for a user
- Supports filtering by status and payment status
- Supports sorting by:
  - Order date
  - Total amount
  - Status
  - Payment status

### 4. Order Status Management

```java
@PatchMapping("/{orderId}/status")
public ResponseEntity<OrderResponse> updateOrderStatus(
    @PathVariable Long orderId,
    @RequestParam String status)
```

- Updates order status
- Supports status transitions

### 5. Order Cancellation

```java
@PostMapping("/{orderId}/cancel")
public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId)
```

- Cancels existing orders
- Handles cancellation logic

## Data Model

### Order Entity

```java
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String addressId;

    @Column(nullable = true)
    private String paymentMethod;
}
```

## Pending Implementation

### 1. Order Tracking

- [ ] Order tracking number generation
- [ ] Delivery status updates
- [ ] Tracking information API endpoints
- [ ] Integration with shipping providers

### 2. Order Notifications

- [ ] Email notifications for order status changes
- [ ] SMS notifications for critical updates
- [ ] Push notifications for mobile app

### 3. Order Analytics

- [ ] Order statistics and metrics
- [ ] Sales reports
- [ ] Customer order history analysis

### 4. Order Returns & Refunds

- [ ] Return request processing
- [ ] Refund processing
- [ ] Return shipping label generation
- [ ] Return status tracking

### 5. Order Export

- [ ] Export orders to CSV/Excel
- [ ] Bulk order operations
- [ ] Order data backup

### 6. Advanced Features

- [ ] Order splitting for multiple shipments
- [ ] Partial order cancellation
- [ ] Order modification after placement
- [ ] Reorder functionality

## API Endpoints

### Implemented Endpoints

1. `POST /api/orders` - Create new order
2. `GET /api/orders/{orderId}` - Get order by ID
3. `GET /api/orders/user/{userId}` - Get user's orders
4. `PATCH /api/orders/{orderId}/status` - Update order status
5. `POST /api/orders/{orderId}/cancel` - Cancel order

### Pending Endpoints

1. `GET /api/orders/{orderId}/tracking` - Get order tracking info
2. `POST /api/orders/{orderId}/return` - Initiate return
3. `GET /api/orders/analytics` - Get order analytics
4. `POST /api/orders/export` - Export orders
5. `PATCH /api/orders/{orderId}/modify` - Modify order
6. `POST /api/orders/{orderId}/reorder` - Reorder items
