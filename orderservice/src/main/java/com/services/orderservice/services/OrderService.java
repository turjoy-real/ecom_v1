package com.services.orderservice.services;

import com.services.orderservice.dtos.OrderRequestDTO;
import com.services.orderservice.dtos.OrderResponseDTO;
import com.services.orderservice.dtos.OrderStatusUpdateDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO orderRequest);
    OrderResponseDTO getOrderByNumber(String orderNumber);
    List<OrderResponseDTO> getOrdersByUser(String userId);
    OrderResponseDTO updateOrderStatus(OrderStatusUpdateDTO statusUpdate);
}
