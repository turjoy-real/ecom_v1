package com.services.orderservice.repositories;

import com.services.orderservice.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all order items by order ID
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Find all order items by product ID
     */
    List<OrderItem> findByProductId(String productId);

    /**
     * Delete all order items by order ID
     */
    void deleteByOrderId(Long orderId);

    /**
     * Count order items by order ID
     */
    long countByOrderId(Long orderId);
}