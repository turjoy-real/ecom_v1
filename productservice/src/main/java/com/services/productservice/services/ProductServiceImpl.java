package com.services.productservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.services.productservice.dtos.ProductRequest;
import com.services.productservice.dtos.ProductResponse;
import com.services.productservice.exceptions.IncompleteProductInfo;
import com.services.productservice.exceptions.ProductNotFoundException;
import com.services.productservice.models.Category;
import com.services.productservice.models.CategoryDocument;
import com.services.productservice.models.Product;
import com.services.productservice.models.ProductDocument;
import com.services.productservice.repositories.CategoryRepository;
import com.services.productservice.repositories.ProductElasticsearchRepository;
import com.services.productservice.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
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
        logger.info("Category passed: {}", request.getCategory());
        Category category = categoryRepository.findByName(request.getCategory()).orElseGet(() -> {
            Category category1 = new Category();
            category1.setName(request.getCategory());
            return categoryRepository.save(category1);
        });
        logger.info("Final Category: {}", category.toString());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .brand(request.getBrand())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .build();

        Product savedProduct = productRepository.save(product);

        ProductDocument productDocument = ProductDocument.builder()
                .id(savedProduct.getId().toString())
                .name(savedProduct.getName())
                .description(savedProduct.getDescription())
                .price(savedProduct.getPrice())
                .category(CategoryDocument.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .brand(savedProduct.getBrand())
                .stockQuantity(savedProduct.getStockQuantity())
                .imageUrl(savedProduct.getImageUrl())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            logger.info("Saving to Elasticsearch:\n{}",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(productDocument));
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize productDocument", e);
        }
        logger.info("Saving to elastic search: {}", category.toString());

        productElasticsearchRepository.save(productDocument);
        return mapToResponse(savedProduct);
    }

    @Override
    public List<ProductResponse> getAllProducts(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        try {
            return productRepository.findAll(pageable).stream().map(this::mapToResponse).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("get all products err:");
            System.err.println(e);
            return new ArrayList<>();
        }

    }

    @Override
    public ProductResponse getProductById(Long id) {
        if (cacheService.isProductCached(redisTemplate, id)) {
            logger.info("Product found in cache");
            try {
                ProductResponse product = cacheService.getProductFromCache(redisTemplate, id);
                return product;
            } catch (Exception e) {
                cacheService.deleteProductFromCache(redisTemplate, id);
            }
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            logger.info("Product found with id {}:\n{}", id,
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product));
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize product 1.1: {}", e, product.getCategory().toString());
        }
        // return product;
        ProductResponse response = mapToResponse(product);
        cacheService.cacheProduct(redisTemplate, id, response);
        return response;
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        return productElasticsearchRepository.findByNameFuzzy(keyword, Pageable.unpaged()).stream()
                .map(this::mapToResponseFromDocument)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(String categoryStr, int pageNumber, int pageSize) {
        Category category = categoryRepository.findByName(categoryStr).orElseGet(() -> {
            Category category1 = new Category();
            category1.setName(categoryStr);
            return categoryRepository.save(category1);
        });
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return productRepository.findByCategory(category, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public void updateProduct(Long id, ProductRequest request) {
        Category category = categoryRepository.findByName(request.getCategory()).orElseGet(() -> {
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
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Remove category reference before deletion
        product.setCategory(null);
        productRepository.save(product);

        // Now delete the product
        productRepository.deleteById(id);
        productElasticsearchRepository.deleteById(id.toString());
    }

    private ProductResponse mapToResponse(Product product) {

        // Category category =
        // categoryRepository.findByName(product.getCategory().getName())
        // .orElseThrow(() -> new IncompleteProductInfo("Category data is incomplete"));

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory() != null ? product.getCategory().getName() : null)
                .brand(product.getBrand())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .build();
    }

    private ProductResponse mapToResponseFromDocument(ProductDocument product) {
        // Category category =
        // categoryRepository.findById(product.getCategory().getId())
        // .orElseGet(() -> {
        // Category category1 = new Category();
        // category1.setName(product.getCategory().getName());
        // return categoryRepository.save(category1);
        // });
        return ProductResponse.builder()
                .id(Long.valueOf(product.getId()))
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory() != null ? product.getCategory().getName() : null)
                .brand(product.getBrand())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .build();
    }

    // New search methods with pagination
    public Page<ProductResponse> searchProductsWithPagination(String keyword, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return productElasticsearchRepository.findByNameFuzzy(keyword, pageable)
                .map(this::mapToResponseFromDocument);
    }

    public Page<ProductResponse> searchByCategory(String categoryName, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return productElasticsearchRepository.findByCategoryName(categoryName, pageable)
                .map(this::mapToResponseFromDocument);
    }

    public Page<ProductResponse> searchByBrand(String brand, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return productElasticsearchRepository.findByBrand(brand, pageable)
                .map(this::mapToResponseFromDocument);
    }

    public Page<ProductResponse> searchByPriceRange(Double minPrice, Double maxPrice, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return productElasticsearchRepository.findByPriceBetween(minPrice, maxPrice, pageable)
                .map(this::mapToResponseFromDocument);
    }

    public Page<ProductResponse> searchByMultipleCriteria(
            String categoryName,
            String brand,
            Double minPrice,
            Double maxPrice,
            int pageNumber,
            int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return productElasticsearchRepository.findByCategoryNameAndBrandAndPriceBetween(
                categoryName, brand, minPrice, maxPrice, pageable)
                .map(this::mapToResponseFromDocument);
    }

    public Page<ProductResponse> searchByStockAvailability(Integer minQuantity, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return productElasticsearchRepository.findByStockQuantityGreaterThan(minQuantity, pageable)
                .map(this::mapToResponseFromDocument);
    }

    public Page<ProductResponse> fullTextSearch(String text, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return productElasticsearchRepository.searchByText(text, pageable)
                .map(this::mapToResponseFromDocument);
    }
}
