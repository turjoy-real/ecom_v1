package com.services.oauthserver.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.services.oauthserver.models.EmailVerificationToken;
import com.services.oauthserver.models.User;
import com.services.oauthserver.repositories.EmailVerificationTokenRepository;
import com.services.oauthserver.repositories.UserRepo;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@EnableScheduling
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepo;
    private final UserRepo userRepo;
    private final EmailEventProducer emailEventProducer;

    @Value("${app.frontend.base-url:http://localhost:9001}")
    private String baseUrl;

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepo,
            UserRepo userRepo,
            EmailEventProducer emailEventProducer) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
        this.emailEventProducer = emailEventProducer;
    }

    // @Transactional
    // public void sendVerificationEmail(User user) {
    // String token = UUID.randomUUID().toString();
    // EmailVerificationToken verificationToken = new EmailVerificationToken(
    // token, user, LocalDateTime.now().plusDays(1));
    // tokenRepo.save(verificationToken);
    // String link = baseUrl + "/api/users/open/verify-email?token=" + token;
    // emailEventProducer.sendVerificationEmail(user.getEmail(), link);
    // }

    @Transactional
    public void sendVerificationEmail(User user) {
        // Delete any existing tokens for this user
        tokenRepo.deleteByUser(user);

        // Generate new token
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken(
                token,
                user,
                LocalDateTime.now().plusHours(4)); // 4 hours expiration
        tokenRepo.save(verificationToken);

        String link = baseUrl + "/api/users/open/verify-email?token=" + verificationToken.getToken();
        emailEventProducer.sendVerificationEmail(user.getEmail(), link);
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

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepo.deleteAllByExpiryBefore(LocalDateTime.now());
    }
}
