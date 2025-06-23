package com.services.orderservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "productservice")
public interface ProductClient {

    @GetMapping("/api/products/{id}/verify-stock")
    Boolean verifyStock(@PathVariable("id") Long productId, @RequestParam("quantity") int quantity);

}
