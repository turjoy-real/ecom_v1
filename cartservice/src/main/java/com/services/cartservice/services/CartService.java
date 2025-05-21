package com.services.cartservice.services;

import com.services.cartservice.dtos.CartItemDTO;
import com.services.cartservice.dtos.CartResponse;

/**
 * Interface defining the contract for cart operations
 */
public interface CartService {

    /**
     * Retrieves the cart for a given user
     * 
     * @param userId the ID of the user
     * @return CartResponse containing the user's cart items and total
     */
    CartResponse getCart(String userId);

    /**
     * Adds an item to the user's cart or updates quantity if item already exists
     * 
     * @param userId      the ID of the user
     * @param cartItemDTO the item to add to the cart
     * @return CartResponse containing the updated cart
     */
    CartResponse addItemToCart(String userId, CartItemDTO cartItemDTO);

    /**
     * Removes a specific item from the user's cart
     * 
     * @param userId    the ID of the user
     * @param productId the ID of the product to remove
     */
    void removeItemFromCart(String userId, String productId);

    /**
     * Clears all items from the user's cart
     * 
     * @param userId the ID of the user
     */
    void clearCart(String userId);

    /**
     * Decrements the quantity of a specific item in the user's cart
     * 
     * @param userId    the ID of the user
     * @param productId the ID of the product to decrement
     * @return CartResponse containing the updated cart
     */
    CartResponse decrementItemQuantity(String userId, String productId);
}
