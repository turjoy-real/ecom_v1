package com.services.cartservice.controllers;

import com.services.cartservice.services.CartService;
import com.services.common.dtos.CartItemDTO;
import com.services.common.dtos.CartResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
public class CartController {
    private final CartService cartService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        String userId = authentication.getName(); // or extract from principal
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartResponse> addItemToCart(
            Authentication authentication,
          @Valid  @RequestBody CartItemDTO cartItemDTO) {
        String userId = authentication.getName();

        System.out.println("Authenticated User: " + userId);
        return ResponseEntity.ok(cartService.addItemToCart(userId, cartItemDTO));
    }

    // ➖ Decrement item quantity
    @PatchMapping("/items/{productId}/decrement")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartResponse> decrementItemQuantity(
            Authentication authentication,
            @PathVariable String productId) {
        String userId = authentication.getName();
        return ResponseEntity.ok(cartService.decrementItemQuantity(userId, productId));
    }

    // ❌ Remove item from cart
    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeItemFromCart(
            Authentication authentication,
            @PathVariable String productId) {
        String userId = authentication.getName();
        cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.noContent().build();
    }

    // 🧹 Clear entire cart
    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        String userId = authentication.getName();
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}