package com.services.orderservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "productservice")
public interface ProductClient {

    @GetMapping("/api/products/{id}/verify-stock")
    Boolean verifyStock(@PathVariable("id") Long productId, @RequestParam("quantity") int quantity);

    @PostMapping("/api/products/{id}/reduce-stock")
    void reduceStock(@PathVariable("id") Long productId, @RequestParam("quantity") int quantity, @RequestHeader("Authorization") String token);

    @PostMapping("/api/products/{id}/replenish-stock")
    void replenishStock(@PathVariable("id") Long productId, @RequestParam("quantity") int quantity, @RequestHeader("Authorization") String token);

}
