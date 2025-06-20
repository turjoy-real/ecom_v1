package com.services.cartservice.controllers;

import com.services.cartservice.dtos.CartItemDTO;
import com.services.cartservice.dtos.CartResponse;
import com.services.cartservice.services.CartService;
import com.services.cartservice.services.OrderServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final OrderServiceClient orderServiceClient;

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
            @RequestBody CartItemDTO cartItemDTO) {
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

    @PostMapping("/place-order")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> placeOrder(Authentication authentication, @RequestParam String addressId) {
        String userId = authentication.getName();
        CartResponse cart = cartService.getCart(userId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty");
        }
        String paymentLink = orderServiceClient.placeOrder(userId, addressId, cart.getItems());
        cartService.clearCart(userId);
        return ResponseEntity.ok(paymentLink);
    }
}