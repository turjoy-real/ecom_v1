package com.services.orderservice.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

import com.services.orderservice.models.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(String userId);
    Order findByOrderNumber(String orderNumber);
}