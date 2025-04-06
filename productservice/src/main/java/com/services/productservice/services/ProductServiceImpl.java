package com.services.productservice.services;

import com.services.productservice.dtos.ProductRequest;
import com.services.productservice.dtos.ProductResponse;
import com.services.productservice.models.Category;
import com.services.productservice.models.Product;
import com.services.productservice.models.ProductDocument;
import com.services.productservice.repositories.CategoryRepository;
import com.services.productservice.repositories.ProductElasticsearchRepository;
import com.services.productservice.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductElasticsearchRepository productElasticsearchRepository;
    private final CategoryRepository categoryRepository;
    // private final RestTemplate restTemplate;
    private final RedisTemplate<Long, Object> redisTemplate;
    private final CacheService cacheService;

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findByName(request.getName()).orElseGet(() -> {
            Category category1 = new Category();
            category1.setName(request.getName());
            return categoryRepository.save(category1);
        });
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .brand(request.getBrand())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .build();

        productRepository.save(product);

        ProductDocument productDocument = ProductDocument.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(category)
                .brand(product.getBrand())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .build();

        productElasticsearchRepository.save(productDocument);
        return mapToResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return productRepository.findAll(pageable).stream().map(this::mapToResponse).collect(Collectors.toList());
        // return productRepository.findAll().stream()
        // .map(this::mapToResponse)
        // .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Long id) {
        // ResponseEntity<String> responseEntity =
        // restTemplate.getForEntity("http://userservice/users/1", String.class);
        if (cacheService.isProductCached(redisTemplate, id)) {
            logger.info("Product found in cache");
            Product product = cacheService.getProductFromCache(redisTemplate, id);
            return mapToResponse(product);
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        cacheService.cacheProduct(redisTemplate, id, product);
        return mapToResponse(product);
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {

        return productElasticsearchRepository.findByNameFuzzy(keyword).stream()
                .map(this::mapToResponseFromDocument)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String categoryStr) {
        Category category = categoryRepository.findByName(categoryStr).orElseGet(() -> {
            Category category1 = new Category();
            category1.setName(categoryStr);
            return categoryRepository.save(category1);
        });
        return productRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateProduct(Long id, ProductRequest request) {
        Category category = categoryRepository.findByName(request.getName()).orElseGet(() -> {
            Category category1 = new Category();
            category1.setName(request.getName());
            return categoryRepository.save(category1);
        });
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(category);
        product.setBrand(request.getBrand());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());

        productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
        productElasticsearchRepository.deleteById(id.toString());
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .brand(product.getBrand())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .build();
    }

    private ProductResponse mapToResponseFromDocument(ProductDocument product) {
        Category category = categoryRepository.findByName(product.getName()).orElseGet(() -> {
            Category category1 = new Category();
            category1.setName(product.getName());
            return categoryRepository.save(category1);
        });
        return ProductResponse.builder()
                .id(Long.valueOf(product.getId())) // Convert String ID from Elasticsearch to Long
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(category)
                .brand(product.getBrand())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
