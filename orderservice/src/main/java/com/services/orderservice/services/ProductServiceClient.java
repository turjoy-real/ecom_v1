package com.services.orderservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class ProductServiceClient {
    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductServiceClient(
            RestTemplate restTemplate,
            @Value("${product.service.url:http://productservice}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    public boolean verifyStock(String productId, int quantity) {
        try {
            String url = productServiceUrl + "/api/products/" + productId + "/verify-stock?quantity=" + quantity;
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Boolean.class);

            return response.getStatusCode() == HttpStatus.OK && Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify product stock: " + e.getMessage(), e);
        }
    }

    public ProductDetails getProductDetails(String productId) {
        try {
            String url = productServiceUrl + "/api/products/" + productId;
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ProductDetails> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ProductDetails.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("Product not found: " + productId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Product not found: " + productId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get product details: " + e.getMessage(), e);
        }
    }

    public static class ProductDetails {
        private String id;
        private String name;
        private double price;
        private int stockQuantity;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getStockQuantity() {
            return stockQuantity;
        }

        public void setStockQuantity(int stockQuantity) {
            this.stockQuantity = stockQuantity;
        }
    }
}