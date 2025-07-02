package com.services.paymentservice.repositories;

import com.services.common.enums.PaymentStatus;
import com.services.paymentservice.models.Payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(String userId);

    List<Payment> findByOrderId(String orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByUserIdAndStatus(String userId, PaymentStatus status);
}