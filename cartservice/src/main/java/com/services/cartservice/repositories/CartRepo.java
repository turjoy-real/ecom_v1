package com.services.cartservice.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.services.cartservice.models.CartItem;

@Repository
public interface CartRepo extends MongoRepository<CartItem, String> {
    List<CartItem> findByUserId(String userId);
    void deleteByUserIdAndProductId(String userId, String productId);
    CartItem findByUserIdAndProductId(String userId, String productId);
    void deleteByUserId(String userId);
}
