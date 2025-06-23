# 1. Product Catalog Module

## 1.1 Data Layer

### 1.1.1 Entity Design

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

**Key Design Decisions:**

- **Lazy Loading for Category:** Reduces initial query load and improves performance for product listings. Category details are loaded only when needed, preventing N+1 query problems.
- **Cascade Operations:** Category deletion cascades to products, ensuring data consistency and preventing orphaned records.
- **BaseModel Extension:** Common fields (id, timestamps) for audit trail support and consistent entity structure.

---

### 1.1.2 Repository Layer

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(Category category, Pageable pageable);
    List<Product> findByBrand(String brand);
    List<Product> findByNameContainingIgnoreCase(String keyword);
    Page<Product> findByStockQuantityGreaterThanEqual(Integer minQuantity, Pageable pageable);
    Page<Product> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);
}
```

**Advanced Query Features:**

- **Pagination Support:** Efficient handling of large datasets, memory optimization, and better user experience.
- **Dynamic Queries:** Flexible search capabilities, case-insensitive and partial text matching.
- **Range Queries:** Price range and stock quantity filtering.
- **Category-based Filtering:** Products filtered by category with pagination.

---

## 1.2 Search Implementation

### 1.2.1 JPA-Based Search (Current Implementation)

The current implementation uses Spring Data JPA for search functionality, providing:

- **Full-text search** using JPA criteria queries
- **Multiple search criteria** support
- **Pagination** for all search operations
- **Case-insensitive** search
- **Dynamic query building** based on provided parameters

```java
@Service
public class ProductService {

public Page<ProductResponse> searchByMultipleCriteria(
        String categoryName,
        String brand,
        Double minPrice,
        Double maxPrice,
        int pageNumber,
        int pageSize) {

        Specification<Product> spec = Specification.where(null);

    if (categoryName != null) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("category").get("name")),
                       "%" + categoryName.toLowerCase() + "%"));
    }

    if (brand != null) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("brand")),
                       "%" + brand.toLowerCase() + "%"));
    }

    if (minPrice != null || maxPrice != null) {
            spec = spec.and((root, query, cb) -> {
                if (minPrice != null && maxPrice != null) {
                    return cb.between(root.get("price"), minPrice, maxPrice);
                } else if (minPrice != null) {
                    return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
                } else {
                    return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
                }
            });
        }

        return productRepository.findAll(spec, PageRequest.of(pageNumber, pageSize))
                .map(this::mapToProductResponse);
    }
}
```

**Search Features:**

- **Keyword Search:** Full-text search across product name and description
- **Category Filtering:** Filter products by category name
- **Brand Filtering:** Filter products by brand
- **Price Range Filtering:** Filter products by price range
- **Stock Availability:** Filter products by minimum stock quantity
- **Advanced Multi-criteria Search:** Combine multiple search criteria
- **Pagination:** All search results support pagination

---

## 1.3 API Endpoints

### 1.3.1 Product Management

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    // Create new product
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request)

    // Get all products with pagination
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize)

    // Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id)

    // Verify stock availability
    @GetMapping("/{id}/verify-stock")
    public ResponseEntity<Boolean> verifyStock(@PathVariable Long id, @RequestParam int quantity)

    // Update product
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request)

    // Delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id)
}
```

### 1.3.2 Search Endpoints

```java
// Keyword search
@GetMapping("/search")
public ResponseEntity<Page<ProductResponse>> searchProducts(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize)

// Category-based search
@GetMapping("/search/category")
public ResponseEntity<Page<ProductResponse>> searchByCategory(
        @RequestParam String categoryName,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize)

// Brand-based search
@GetMapping("/search/brand")
public ResponseEntity<Page<ProductResponse>> searchByBrand(
        @RequestParam String brand,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize)

// Price range search
@GetMapping("/search/price")
public ResponseEntity<Page<ProductResponse>> searchByPriceRange(
        @RequestParam Double minPrice,
        @RequestParam Double maxPrice,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize)

// Advanced multi-criteria search
@GetMapping("/search/advanced")
public ResponseEntity<Page<ProductResponse>> searchByMultipleCriteria(
        @RequestParam(required = false) String categoryName,
        @RequestParam(required = false) String brand,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize)

// Stock availability search
@GetMapping("/search/stock")
public ResponseEntity<Page<ProductResponse>> searchByStockAvailability(
        @RequestParam Integer minQuantity,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize)

// Full-text search
@GetMapping("/search/full-text")
public ResponseEntity<Page<ProductResponse>> fullTextSearch(
        @RequestParam String text,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize)

// Get products by category
@GetMapping("/category/{category}")
public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
        @PathVariable String category,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize)
```

---

## 1.4 Caching Strategy

### 1.4.1 Redis Implementation (Planned)

```java
@Service
public class CacheService {
    private String productKey = "PRODUCTS";

    public boolean isProductCached(RedisTemplate<Long, Object> redisTemplate, Long id) {
        return redisTemplate.opsForHash().hasKey(id, productKey);
    }

    public void updateProductInCache(RedisTemplate<Long, Object> redisTemplate, Long id, Product product) {
        redisTemplate.opsForHash().put(id, productKey, product);
    }

    public void deleteProductFromCache(RedisTemplate<Long, Object> redisTemplate, Long id) {
        redisTemplate.opsForHash().delete(id, productKey);
    }

    public ProductResponse getProductFromCache(RedisTemplate<Long, Object> redisTemplate, Long id) {
        return (ProductResponse) redisTemplate.opsForHash().get(id, productKey);
    }

    @Async
    public void cacheProduct(RedisTemplate<Long, Object> redisTemplate, Long id, ProductResponse product) {
        redisTemplate.opsForHash().put(id, productKey, product);
    }
}
```

**Caching Features:**

- **TTL Management:** Configurable cache duration, automatic cache invalidation, memory optimization.
- **Cache Patterns:** Cache-Aside pattern, write-through pattern for updates, cache invalidation on updates.

---

## 1.5 Future Enhancements

### 1.5.1 Elasticsearch Integration (Planned)

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

**Planned Search Features:**

- **Advanced Full-Text Search:** Elasticsearch integration for better search performance
- **Fuzzy Search:** Typo-tolerant search capabilities
- **Aggregations:** Faceted search and filtering
- **Search Suggestions:** Auto-complete functionality
- **Search Analytics:** Search behavior tracking

### 1.5.2 Additional Features

- [ ] Product image management service
- [ ] Product variant handling (size, color, etc.)
- [ ] Real-time inventory management
- [ ] Product recommendation system
- [ ] Product reviews and ratings
- [ ] Product import/export functionality
- [ ] Bulk product operations
- [ ] Product analytics and reporting

---

### References

- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Data Elasticsearch](https://spring.io/projects/spring-data-elasticsearch) (for future implementation)
- [Redis Caching in Spring](https://docs.spring.io/spring-data/redis/docs/current/reference/html/#reference)
