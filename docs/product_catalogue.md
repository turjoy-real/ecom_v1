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
}
```

**Advanced Query Features:**

- **Pagination Support:** Efficient handling of large datasets, memory optimization, and better user experience.
- **Dynamic Queries:** Flexible search capabilities, case-insensitive and partial text matching.

---

## 1.2 Search Implementation

### 1.2.1 Elasticsearch Integration

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

**Search Features:**

- **Full-Text Search:** Standard analyzer for text fields, keyword fields for exact matching, numeric fields for range queries.
- **Advanced Queries:**

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

---

## 1.3 Caching Strategy

### 1.3.1 Redis Implementation

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

### References

- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Data Elasticsearch](https://spring.io/projects/spring-data-elasticsearch)
- [Redis Caching in Spring](https://docs.spring.io/spring-data/redis/docs/current/reference/html/#reference)
