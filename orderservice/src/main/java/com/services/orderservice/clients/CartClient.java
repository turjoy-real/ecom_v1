package com.services.orderservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.services.common.dtos.CartResponse;

@FeignClient(name = "cartservice")
public interface CartClient {

    @GetMapping("/api/cart")
    ResponseEntity<CartResponse> getCart(@RequestHeader("Authorization") String token);

    @DeleteMapping("/api/cart")
    void clearCart(@RequestHeader("Authorization") String token);
}