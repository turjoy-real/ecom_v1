package com.services.cartservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.services.common.dtos.ProductResponse;

@FeignClient(name = "productservice")
public interface ProductClient {

    @GetMapping("/api/products/{productId}/verify-stock")
    ResponseEntity<Boolean> verifyStock(
        @PathVariable("productId") String productId,
        @RequestParam("quantity") int quantity
    );

    @GetMapping("/api/products/{productId}")
    ResponseEntity<ProductResponse> getProduct(
        @PathVariable("productId") String productId
    );

} 