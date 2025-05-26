package com.services.notificationservice.Consumers;

import com.services.notificationservice.adapters.EmailAdapter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationConsumer {

    private final EmailAdapter emailAdapter;

    public EmailVerificationConsumer(EmailAdapter emailAdapter) {
        this.emailAdapter = emailAdapter;
    }

    @KafkaListener(topics = "email-verification", groupId = "email-verification-group")
    public void handleEmailVerification(String msg) {
        String[] parts = msg.split(" // ");

        String to = parts[0];
        String link = parts[1];

        String subject = "Verify Your Email";
        String message = "Please click the following link to verify your email: " + link;

        emailAdapter.sendMsg(to, message, subject, null);
    }
}