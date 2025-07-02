package com.services.cartservice.services;

import com.services.cartservice.clients.ProductClient;
import com.services.cartservice.exceptions.InsufficientStockException;
import com.services.cartservice.models.CartItem;
import com.services.cartservice.repositories.CartRepo;
import com.services.common.dtos.CartItemDTO;
import com.services.common.dtos.CartResponse;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepo cartRepository;
    private final ProductClient productClient;



    @Override
    @Cacheable(value = "cart", key = "#userId")
    public CartResponse getCart(String userId) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        return buildCartResponse(userId, cartItems);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(String userId, CartItemDTO cartItemDTO) {

        // Verify stock before adding to cart
        if (!productClient.verifyStock(cartItemDTO.getProductId(), cartItemDTO.getQuantity()).getBody()) {
            throw new InsufficientStockException("Insufficient stock for product: " + cartItemDTO.getProductId());
        }

        CartItem existingItem = cartRepository.findByUserIdAndProductId(userId, cartItemDTO.getProductId());

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + cartItemDTO.getQuantity();
            if (!productClient.verifyStock(cartItemDTO.getProductId(), newQuantity).getBody()) {
                throw new InsufficientStockException("Insufficient stock for product: " + cartItemDTO.getProductId());
            }
            existingItem.setQuantity(newQuantity);
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
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public void removeItemFromCart(String userId, String productId) {
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    @CachePut(value = "cart", key = "#userId")
    public CartResponse decrementItemQuantity(String userId, String productId) {

        CartItem existingItem = cartRepository.findByUserIdAndProductId(userId, productId);

        if (existingItem == null) {
            throw new RuntimeException("Item not found in cart");
        }

        if (existingItem.getQuantity() > 1) {
            existingItem.setQuantity(existingItem.getQuantity() - 1);
            cartRepository.save(existingItem);
        } else {
            // If quantity is 1, remove the item completely
            cartRepository.deleteByUserIdAndProductId(userId, productId);
        }

        return getCart(userId);
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