package com.services.orderservice.repositories;

import com.services.orderservice.models.OrderTracking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTrackingRepository extends JpaRepository<OrderTracking, Long> {
    OrderTracking findByOrderId(Long orderId);
}