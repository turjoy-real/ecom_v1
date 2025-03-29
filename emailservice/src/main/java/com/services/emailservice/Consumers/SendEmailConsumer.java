package com.services.emailservice.Consumers;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SendEmailConsumer {

    @KafkaListener(topics = "send-email", groupId = "email_group")
    public void handleSendEmail(String email) {
        // Logic to send email
        System.out.println("Sending email to: " + email);
    }
}
