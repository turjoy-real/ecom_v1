package com.services.orderservice.services;

import com.services.orderservice.dtos.OrderRequest;
import com.services.orderservice.dtos.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrder(Long orderId);

    List<OrderResponse> getUserOrders(String userId, String status, String paymentStatus, String sortBy,
            String sortDirection);

    OrderResponse updateOrderStatus(Long orderId, String status);

    void cancelOrder(Long orderId);
}
