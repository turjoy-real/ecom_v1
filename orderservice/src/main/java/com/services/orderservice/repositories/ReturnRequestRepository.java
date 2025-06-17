package com.services.orderservice.repositories;

import com.services.orderservice.models.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    ReturnRequest findByOrderId(Long orderId);

    List<ReturnRequest> findByStatus(String status);
}