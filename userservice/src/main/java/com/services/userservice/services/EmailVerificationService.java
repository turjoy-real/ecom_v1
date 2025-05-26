package com.services.userservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.services.userservice.models.EmailVerificationToken;
import com.services.userservice.models.User;
import com.services.userservice.repositories.EmailVerificationTokenRepository;
import com.services.userservice.repositories.UserRepo;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepo;
    private final UserRepo userRepo;
    private final EmailEventProducer emailEventProducer;

    @Value("${app.frontend.base-url:http://http://localhost:9001}")
    private String baseUrl;

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepo,
            UserRepo userRepo,
            EmailEventProducer emailEventProducer) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
        this.emailEventProducer = emailEventProducer;
    }

    @Transactional
    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken(
                token, user, LocalDateTime.now().plusDays(1));
        tokenRepo.save(verificationToken);
        String link = baseUrl + "/api/users/open/verify-email?token=" + token;
        emailEventProducer.sendVerificationEmail(user.getEmail(), link);

        // TODO: Add exception handler
    }

    @Transactional
    public boolean verify(String token) {
        Optional<EmailVerificationToken> opt = tokenRepo.findByToken(token);
        if (opt.isEmpty())
            return false;

        EmailVerificationToken verificationToken = opt.get();
        if (verificationToken.isExpired()) {
            tokenRepo.delete(verificationToken);
            return false;
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepo.save(user);
        tokenRepo.delete(verificationToken);
        return true;
    }
}
