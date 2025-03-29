package com.services.orderservice.services;


import com.services.orderservice.dtos.*;
import com.services.orderservice.events.OrderCreatedEvent;
import com.services.orderservice.events.OrderStatusEvent;
import com.services.orderservice.models.*;
import com.services.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequest) {
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(orderRequest.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.CREATED);
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        
        List<OrderItem> items = orderRequest.getItems().stream()
                .map(itemDto -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(itemDto.getProductId());
                    item.setProductName(itemDto.getProductName());
                    item.setPrice(itemDto.getPrice());
                    item.setQuantity(itemDto.getQuantity());
                    item.setOrder(order);
                    return item;
                })
                .collect(Collectors.toList());
        
        order.setItems(items);
        
        double total = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        order.setTotalAmount(total);
        
        Order savedOrder = orderRepository.save(order);
        
        // Publish OrderCreatedEvent
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderNumber(savedOrder.getOrderNumber());
        event.setUserId(savedOrder.getUserId());
        event.setTotalAmount(savedOrder.getTotalAmount());
        event.setItems(orderRequest.getItems());
        event.setPaymentMethod(savedOrder.getPaymentMethod());
        
        kafkaTemplate.send("order-created-topic", event);
        
        return mapToOrderResponse(savedOrder);
    }
    
    @Override
    public OrderResponseDTO getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber);
        return mapToOrderResponse(order);
    }
    
    @Override
    public List<OrderResponseDTO> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(OrderStatusUpdateDTO statusUpdate) {
        Order order = orderRepository.findByOrderNumber(statusUpdate.getOrderNumber());
        order.setStatus(statusUpdate.getNewStatus());
        
        if (statusUpdate.getTrackingNumber() != null) {
            order.setTrackingNumber(statusUpdate.getTrackingNumber());
        }
        
        if (statusUpdate.getEstimatedDelivery() != null) {
            order.setEstimatedDelivery(statusUpdate.getEstimatedDelivery());
        }
        
        Order updatedOrder = orderRepository.save(order);
        
        // Publish OrderStatusEvent
        OrderStatusEvent event = new OrderStatusEvent();
        event.setOrderNumber(updatedOrder.getOrderNumber());
        event.setUserId(updatedOrder.getUserId());
        event.setStatus(updatedOrder.getStatus());
        event.setTrackingNumber(updatedOrder.getTrackingNumber());
        event.setEstimatedDelivery(updatedOrder.getEstimatedDelivery());
        
        kafkaTemplate.send("order-status-updated-topic", event);
        
        return mapToOrderResponse(updatedOrder);
    }
    
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private OrderResponseDTO mapToOrderResponse(Order order) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setUserId(order.getUserId());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setShippingAddress(order.getShippingAddress());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setTrackingNumber(order.getTrackingNumber());
        response.setEstimatedDelivery(order.getEstimatedDelivery());
        
        response.setItems(order.getItems().stream()
                .map(item -> {
                    OrderItemDTO dto = new OrderItemDTO();
                    dto.setProductId(item.getProductId());
                    dto.setProductName(item.getProductName());
                    dto.setPrice(item.getPrice());
                    dto.setQuantity(item.getQuantity());
                    return dto;
                })
                .collect(Collectors.toList()));
        
        return response;
    }
}
