package com.services.orderservice.repositories;

import com.services.orderservice.models.Order;
import com.services.orderservice.models.OrderStatus;
import com.services.orderservice.models.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(String userId);

    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);

    List<Order> findByUserIdAndPaymentStatus(String userId, PaymentStatus paymentStatus);

    List<Order> findByUserIdAndStatusAndPaymentStatus(String userId, OrderStatus status, PaymentStatus paymentStatus);
}