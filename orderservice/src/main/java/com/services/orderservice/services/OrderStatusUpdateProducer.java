package com.services.orderservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusUpdateProducer {
    private static final String TOPIC = "order-status-update";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Async
    public void sendOrderStatusUpdate(String recipientEmail, String orderId, String status) {
        String message = recipientEmail + " // " + orderId + " // " + status;
        kafkaTemplate.send(TOPIC, message);
    }
}