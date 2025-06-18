# 2. Cart Module

## 2.1 Data Storage

### 2.1.1 MongoDB Schema

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

**Design Considerations:**

- **Dual Storage Strategy:** MongoDB for persistence, Redis for performance, eventual consistency model.
- **Optimistic Locking:** Version field for concurrency, prevents race conditions, handles concurrent updates.

---

### 2.1.2 Cart Operations

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

---

## 2.2 Integration Patterns

### 2.2.1 Service Communication

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

---

### References

- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb)
- [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign)
- [Redis Caching in Spring](https://docs.spring.io/spring-data/redis/docs/current/reference/html/#reference)
