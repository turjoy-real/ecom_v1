package com.services.productservice;

import com.services.common.dtos.ProductResponse;
import com.services.productservice.dtos.ProductRequest;
import com.services.productservice.models.Category;
import com.services.productservice.models.Product;
import com.services.productservice.models.ProductDocument;
import com.services.productservice.repositories.CategoryRepository;
import com.services.productservice.repositories.ProductElasticsearchRepository;
import com.services.productservice.repositories.ProductRepository;
import com.services.productservice.services.CacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ProductRepository productRepository;
    @MockBean
    private CategoryRepository categoryRepository;
    @MockBean
    private ProductElasticsearchRepository productElasticsearchRepository;
    @MockBean
    private RedisTemplate<Long, Object> redisTemplate;
    @MockBean
    private CacheService cacheService;

    @Test
    void createProduct_savesToDbAndElasticsearch_andReturnsResponse() {
        ProductRequest productRequest = ProductRequest.builder()
                .name("Integration Product")
                .description("Integration test product")
                .price(123.45)
                .category("IntegrationCategory")
                .brand("IntegrationBrand")
                .stockQuantity(5)
                .imageUrl("http://example.com/integration.jpg")
                .build();

        Category category = Category.builder().name("IntegrationCategory").build();
        category.setId(10L);
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .category(category)
                .brand(productRequest.getBrand())
                .stockQuantity(productRequest.getStockQuantity())
                .imageUrl(productRequest.getImageUrl())
                .build();
        product.setId(100L);

        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productElasticsearchRepository.save(any(ProductDocument.class))).thenReturn(null);

        String url = "http://localhost:" + port + "/api/products";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductRequest> request = new HttpEntity<>(productRequest, headers);
        ResponseEntity<ProductResponse> response = restTemplate.postForEntity(url, request, ProductResponse.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        ProductResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getName()).isEqualTo(productRequest.getName());
        assertThat(body.getDescription()).isEqualTo(productRequest.getDescription());
        assertThat(body.getPrice()).isEqualTo(productRequest.getPrice());
        assertThat(body.getCategory()).isEqualTo(productRequest.getCategory());
        assertThat(body.getBrand()).isEqualTo(productRequest.getBrand());
        assertThat(body.getStockQuantity()).isEqualTo(productRequest.getStockQuantity());
        assertThat(body.getImageUrl()).isEqualTo(productRequest.getImageUrl());
    }

    @Test
    void getProductById_returnsProductResponse() {
        Product product = Product.builder()
                .name("Integration Product")
                .description("Integration test product")
                .price(123.45)
                .category(Category.builder().name("IntegrationCategory").build())
                .brand("IntegrationBrand")
                .stockQuantity(5)
                .imageUrl("http://example.com/integration.jpg")
                .build();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cacheService.isProductCached(any(), anyLong())).thenReturn(false);

        String url = "http://localhost:" + port + "/api/products/1";
        ResponseEntity<ProductResponse> response = restTemplate.getForEntity(url, ProductResponse.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Integration Product");
    }

    @Test
    void updateProduct_returnsNoContent() {
        ProductRequest productRequest = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated test product")
                .price(200.0)
                .category("UpdatedCategory")
                .brand("UpdatedBrand")
                .stockQuantity(10)
                .imageUrl("http://example.com/updated.jpg")
                .build();
        Category category = Category.builder().name("UpdatedCategory").build();
        category.setId(20L);
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .category(category)
                .brand(productRequest.getBrand())
                .stockQuantity(productRequest.getStockQuantity())
                .imageUrl(productRequest.getImageUrl())
                .build();
        product.setId(1L);
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productElasticsearchRepository.findById(anyString())).thenReturn(Optional.empty());
        when(productElasticsearchRepository.save(any(ProductDocument.class))).thenReturn(null);

        String url = "http://localhost:" + port + "/api/products/1";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductRequest> request = new HttpEntity<>(productRequest, headers);
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PATCH, request, Void.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(204);
    }

    @Test
    void deleteProduct_returnsNoContent() {
        Product product = Product.builder().build();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).deleteById(1L);
        doNothing().when(productElasticsearchRepository).deleteById(anyString());

        String url = "http://localhost:" + port + "/api/products/1";
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(204);
    }
} 