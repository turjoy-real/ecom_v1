package com.services.notificationservice.Consumers;

import com.services.notificationservice.adapters.EmailAdapter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetConsumer {

    private final EmailAdapter emailAdapter;

    public PasswordResetConsumer(EmailAdapter emailAdapter) {
        this.emailAdapter = emailAdapter;
    }

    @KafkaListener(topics = "password-reset", groupId = "password-reset-group")
    public void handlePasswordReset(String msg) {
        String[] parts = msg.split(" // ");

        String to = parts[0];
        String link = parts[1];

        String subject = "Reset Your Password";
        String message = "Please click the following link to reset your password: " + link +
                "\n\nIf you did not request a password reset, please ignore this email.";

        emailAdapter.sendMsg(to, message, subject, null);
    }
}