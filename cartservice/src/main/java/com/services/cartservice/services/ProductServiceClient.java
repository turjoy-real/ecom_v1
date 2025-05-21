package com.services.cartservice.services;

import com.services.cartservice.exceptions.InsufficientStockException;
import com.services.cartservice.exceptions.ProductNotFoundException;
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

            if (response.getStatusCode() == HttpStatus.OK && Boolean.TRUE.equals(response.getBody())) {
                return true;
            }
            throw new InsufficientStockException(productId, quantity);

        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException(productId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new InsufficientStockException(productId, quantity);
            }
            throw new RuntimeException("Error verifying product stock: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to communicate with product service: " + e.getMessage(), e);
        }
    }
}