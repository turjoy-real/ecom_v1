package com.services.notificationservice.Consumers;

import com.services.notificationservice.adapters.EmailAdapter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentInvoiceConsumer {
    private final EmailAdapter emailAdapter;

    public PaymentInvoiceConsumer(EmailAdapter emailAdapter) {
        this.emailAdapter = emailAdapter;
    }

    @KafkaListener(topics = "payment-invoice", groupId = "payment-invoice-group")
    public void handlePaymentInvoice(String msg) {
        System.out.println("Received payment invoice message: " + msg);

        String[] parts = msg.split(" // ");
        if (parts.length < 4) {
            System.out.println("Invalid message format. Expected 4 parts, got: " + parts.length);
            return;
        }

        String orderId = parts[0];
        String to = parts[1];
        String amount = parts[2];
        String orderDetails = parts[3];

        System.out.println("Processing invoice for Order ID: " + orderId);
        System.out.println("Sending to email: " + to);
        System.out.println("Amount: " + amount);
        System.out.println("Order Details: " + orderDetails);

        String subject = "Your Payment Invoice for Order #" + orderId;
        String message = "Thank you for your payment.\n\nOrder ID: " + orderId +
                "\nAmount Paid: " + amount +
                "\nOrder Details: " + orderDetails +
                "\n\nIf you have any questions, please contact our support team.";

        emailAdapter.sendMsg(to, message, subject, null);
        System.out.println("Invoice email sent successfully for Order ID: " + orderId);
    }
}