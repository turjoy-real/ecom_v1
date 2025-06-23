package com.services.notificationservice.Consumers;

import com.services.notificationservice.adapters.EmailAdapter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusUpdateConsumer {

    private final EmailAdapter emailAdapter;

    public OrderStatusUpdateConsumer(EmailAdapter emailAdapter) {
        this.emailAdapter = emailAdapter;
    }

    @KafkaListener(topics = "order-status-update", groupId = "order-status-update-group")
    public void handleOrderStatusUpdate(String msg) {
        String[] parts = msg.split(" // ");
        if (parts.length < 3)
            return;
        String to = parts[0];
        String orderId = parts[1];
        String status = parts[2];

        String subject = "Order Status Update: Order #" + orderId;
        String message = "Your order (ID: " + orderId + ") status has been updated to: " + status + ".\n\n"
                + "Thank you for shopping with us!";

        emailAdapter.sendMsg(to, message, subject, null);
    }
}