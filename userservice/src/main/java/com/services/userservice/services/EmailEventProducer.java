package com.services.userservice.services;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.services.userservice.models.EmailVerificationEvent;
import com.services.userservice.models.PasswordResetEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendVerificationEmail(String to, String link) {
        EmailVerificationEvent event = new EmailVerificationEvent();
        event.setTo(to);
        event.setSubject("Verify your email");
        event.setVerificationLink(link);
        kafkaTemplate.send("email-verification", to + " // " + link);
    }

    public void sendPasswordResetEmail(String to, String link) {
        PasswordResetEvent event = new PasswordResetEvent();
        event.setTo(to);
        event.setSubject("Reset your password");
        event.setResetLink(link);
        kafkaTemplate.send("password-reset", to + " // " + link);
    }
}