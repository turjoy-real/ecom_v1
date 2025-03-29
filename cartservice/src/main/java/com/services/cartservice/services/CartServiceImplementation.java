package com.services.cartservice.services;

import com.services.cartservice.dtos.CartItemDTO;
import com.services.cartservice.dtos.CartResponse;

import com.services.cartservice.dtos.CartItemDTO;
import com.services.cartservice.dtos.CartResponse;
import com.services.cartservice.models.CartItem;
import com.services.cartservice.repositories.CartRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImplementation implements CartService {
    private final CartRepo cartRepository;

    @Override
    @Cacheable(value = "cart", key = "#userId")
    public CartResponse getCart(String userId) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        return buildCartResponse(userId, cartItems);
    }

    @Override
    @CachePut(value = "cart", key = "#userId")
    public CartResponse addItemToCart(String userId, CartItemDTO cartItemDTO) {
        CartItem existingItem = cartRepository.findByUserIdAndProductId(userId, cartItemDTO.getProductId());
        
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + cartItemDTO.getQuantity());
            cartRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(cartItemDTO.getProductId());
            newItem.setProductName(cartItemDTO.getProductName());
            newItem.setPrice(cartItemDTO.getPrice());
            newItem.setQuantity(cartItemDTO.getQuantity());
            newItem.setUserId(userId);
            cartRepository.save(newItem);
        }
        
        return getCart(userId);
    }

    @Override
    @CacheEvict(value = "cart", key = "#userId")
    public void removeItemFromCart(String userId, String productId) {
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    @CacheEvict(value = "cart", key = "#userId")
    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }

    private CartResponse buildCartResponse(String userId, List<CartItem> cartItems) {
        List<CartItemDTO> items = cartItems.stream()
                .map(item -> {
                    CartItemDTO dto = new CartItemDTO();
                    dto.setProductId(item.getProductId());
                    dto.setProductName(item.getProductName());
                    dto.setPrice(item.getPrice());
                    dto.setQuantity(item.getQuantity());
                    return dto;
                })
                .collect(Collectors.toList());
        
        double total = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        CartResponse response = new CartResponse();
        response.setUserId(userId);
        response.setItems(items);
        response.setTotal(total);
        
        return response;
    }
}