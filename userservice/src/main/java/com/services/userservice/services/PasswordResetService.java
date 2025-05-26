package com.services.userservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.services.userservice.exceptions.UserNotFound;
import com.services.userservice.models.PasswordResetToken;
import com.services.userservice.models.User;
import com.services.userservice.repositories.PasswordResetTokenRepository;
import com.services.userservice.repositories.UserRepo;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepo userRepo;
    private final EmailEventProducer emailEventProducer;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${app.frontend.base-url:http://localhost:9001/html}")
    private String baseUrl;

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFound("No user with this email"));

        // Delete any existing tokens for this user
        tokenRepo.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                token, user, LocalDateTime.now().plusMinutes(30));
        tokenRepo.save(resetToken);

        String link = baseUrl + "/reset-password.html?token=" + token;
        emailEventProducer.sendPasswordResetEmail(user.getEmail(), link);
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> opt = tokenRepo.findByToken(token);
        if (opt.isEmpty())
            return false;

        PasswordResetToken resetToken = opt.get();
        if (resetToken.isExpired()) {
            tokenRepo.delete(resetToken);
            return false;
        }

        User user = resetToken.getUser();
        user.setHashedPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepo.save(user);
        tokenRepo.delete(resetToken);
        return true;
    }
}
