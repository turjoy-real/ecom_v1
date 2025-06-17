package com.services.orderservice.services;

import com.services.orderservice.dtos.OrderRequest;
import com.services.orderservice.dtos.OrderResponse;
import com.services.orderservice.dtos.OrderTrackingResponse;
import com.services.orderservice.dtos.ReturnRequestDTO;
import com.services.orderservice.dtos.ReturnRequestResponse;
import java.util.List;
import java.util.Map;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrder(Long orderId);

    List<OrderResponse> getUserOrders(String userId, String status, String paymentStatus, String sortBy,
            String sortDirection);

    OrderResponse updateOrderStatus(Long orderId, String status);

    void cancelOrder(Long orderId);

    // New methods for tracking
    OrderTrackingResponse getOrderTracking(Long orderId);

    OrderTrackingResponse updateOrderTracking(Long orderId, String trackingNumber, String carrier);

    // New methods for returns
    ReturnRequestResponse createReturnRequest(Long orderId, ReturnRequestDTO request);

    ReturnRequestResponse getReturnRequest(Long returnId);

    ReturnRequestResponse updateReturnStatus(Long returnId, String status);

    List<ReturnRequestResponse> getReturnRequestsByStatus(String status);

    // New method for analytics
    Map<String, Object> getOrderAnalytics(String userId);
}
