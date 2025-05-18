package com.services.orderservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.services.orderservice.dtos.OrderRequestDTO;
import com.services.orderservice.dtos.OrderResponseDTO;
import com.services.orderservice.dtos.OrderStatusUpdateDTO;
import com.services.orderservice.dtos.SendEmailEventDTO;
import com.services.orderservice.services.OrderService;
import lombok.RequiredArgsConstructor;

import org.apache.kafka.common.network.Send;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequest) {
        SendEmailEventDTO sendEmailEvent = new SendEmailEventDTO();
        sendEmailEvent.setBody("Order placed");
        sendEmailEvent.setFrom("dev@turjoysaha.com");
        sendEmailEvent.setSubject("Order Confirmation");
        sendEmailEvent.setTo("user@email.com");

        try {
            kafkaTemplate.send("order-email-topic", "Test");
        } catch (Exception e) {
           throw new RuntimeException("Failed to send email event", e);
        }
        
        return ResponseEntity.ok(orderService.createOrder(orderRequest));
    }
    
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(@PathVariable String userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }
    
    @PutMapping("/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@RequestBody OrderStatusUpdateDTO statusUpdate) {
        return ResponseEntity.ok(orderService.updateOrderStatus(statusUpdate));
    }
}