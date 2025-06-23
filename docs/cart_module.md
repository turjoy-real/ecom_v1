# 2. Cart Module

## 2.1 Data Storage

### 2.1.1 MySQL Schema (Current Implementation)

```java
@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cart", orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Version
    private Long version;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;

    @Version
    private Long version;
}
```

**Design Considerations:**

- **MySQL Storage Strategy:** Single database storage for persistence and consistency.
- **Optimistic Locking:** Version field for concurrency control, prevents race conditions, handles concurrent updates.
- **User-specific Carts:** Each user has their own cart with isolated items.
- **Cascade Operations:** Cart item management with proper cleanup.

---

### 2.1.2 Cart Operations

```java
@Service
public class CartServiceImplementation implements CartService {
    @Override
    @Transactional
    public CartResponse addItemToCart(String userId, CartItemDTO cartItemDTO) {
        // Stock verification
        if (!productServiceClient.verifyStock(cartItemDTO.getProductId(), cartItemDTO.getQuantity())) {
            throw new InsufficientStockException("Insufficient stock");
        }

        // Get or create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        // Check if item already exists
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(cartItemDTO.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            return updateExistingItem(existingItem.get(), cartItemDTO);
        } else {
            return addNewItem(cart, cartItemDTO);
        }
    }

    private CartResponse updateExistingItem(CartItem existingItem, CartItemDTO newItem) {
        int newQuantity = existingItem.getQuantity() + newItem.getQuantity();
        if (!productServiceClient.verifyStock(newItem.getProductId(), newQuantity)) {
            throw new InsufficientStockException("Insufficient stock for updated quantity");
        }

        existingItem.setQuantity(newQuantity);
        cartRepository.save(existingItem.getCart());
        return getCart(existingItem.getCart().getUserId());
    }

    private CartResponse addNewItem(Cart cart, CartItemDTO cartItemDTO) {
        CartItem newItem = CartItem.builder()
                .cart(cart)
                .productId(cartItemDTO.getProductId())
                .quantity(cartItemDTO.getQuantity())
                .price(cartItemDTO.getPrice())
                .build();

        cart.getItems().add(newItem);
        cartRepository.save(cart);
        return getCart(cart.getUserId());
    }
}
```

---

## 2.2 API Endpoints

### 2.2.1 Cart Management Endpoints

```java
@RestController
@RequestMapping("/api/cart")
public class CartController {

    // Get user's cart
    @GetMapping
    public ResponseEntity<CartResponse> getCart()

    // Add item to cart
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(@RequestBody CartItemDTO cartItemDTO)

    // Update item quantity
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long productId,
            @RequestParam Integer quantity)

    // Remove item from cart
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItemFromCart(@PathVariable Long productId)

    // Clear entire cart
    @DeleteMapping
    public ResponseEntity<Void> clearCart()
}
```

### 2.2.2 Cart Operations

- **Add Items:** Add products to cart with stock verification
- **Update Quantities:** Modify item quantities with stock validation
- **Remove Items:** Remove specific items from cart
- **Clear Cart:** Remove all items from cart
- **View Cart:** Retrieve current cart contents with product details

---

## 2.3 Integration Patterns

### 2.3.1 Service Communication

```java
@FeignClient(name = "product-service")
public interface ProductServiceClient {
    @GetMapping("/api/products/{id}/verify-stock")
    boolean verifyStock(@PathVariable("id") Long productId, @RequestParam int quantity);

    @GetMapping("/api/products/{id}")
    ProductDetails getProductDetails(@PathVariable("id") Long productId);
}
```

**Integration Features:**

- **Circuit Breaker Pattern:** Resilience against service failures, fallback mechanisms, timeout handling.
- **Load Balancing:** Service discovery integration, dynamic routing, health checks.
- **Stock Verification:** Real-time stock checking before cart operations.
- **Product Details:** Fetching product information for cart display.

---

## 2.4 Data Model

### 2.4.1 Cart Entity

```java
@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cart", orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Version
    private Long version;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Helper methods
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    public double getTotalAmount() {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
```

### 2.4.2 Cart Item Entity

```java
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;

    @Version
    private Long version;

    // Helper methods
    public double getSubtotal() {
        return price * quantity;
    }
}
```

---

## 2.5 Business Logic

### 2.5.1 Stock Verification

```java
@Service
public class CartService {

    private boolean verifyStockAvailability(Long productId, Integer quantity) {
        try {
            return productServiceClient.verifyStock(productId, quantity);
        } catch (Exception e) {
            log.error("Error verifying stock for product {}: {}", productId, e.getMessage());
            // Fallback: assume stock is available to prevent cart blocking
            return true;
        }
    }
}
```

### 2.5.2 Cart Validation

```java
@Service
public class CartValidationService {

    public void validateCartItem(CartItemDTO cartItemDTO) {
        if (cartItemDTO.getQuantity() <= 0) {
            throw new InvalidCartItemException("Quantity must be greater than 0");
        }

        if (cartItemDTO.getPrice() <= 0) {
            throw new InvalidCartItemException("Price must be greater than 0");
        }

        if (cartItemDTO.getProductId() == null) {
            throw new InvalidCartItemException("Product ID is required");
        }
    }
}
```

---

## 2.6 Future Enhancements

### 2.6.1 Planned Features

- [ ] **Redis Caching:** Add Redis caching for improved performance
- [ ] **Cart Expiration:** Implement cart expiration and cleanup
- [ ] **Real-time Updates:** WebSocket-based real-time cart updates
- [ ] **Cart Sharing:** Allow users to share cart contents
- [ ] **Abandoned Cart Recovery:** Email notifications for abandoned carts
- [ ] **Promotional Codes:** Support for discount codes and promotions
- [ ] **Multiple Carts:** Support for wishlist and saved carts
- [ ] **Cart Analytics:** Track cart behavior and conversion rates

### 2.6.2 Performance Optimizations

- [ ] **Caching Layer:** Redis integration for cart data
- [ ] **Batch Operations:** Bulk cart operations for better performance
- [ ] **Database Indexing:** Optimize database queries with proper indexing
- [ ] **Connection Pooling:** Optimize database connection management

---

### References

- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign)
- [Spring Transaction Management](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)
