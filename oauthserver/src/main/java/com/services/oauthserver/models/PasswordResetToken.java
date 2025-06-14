package com.services.oauthserver.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class PasswordResetToken extends BaseModel {
    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiry;

    protected PasswordResetToken() {
    }

    public PasswordResetToken(String token, User user, LocalDateTime expiry) {
        this.token = token;
        this.user = user;
        this.expiry = expiry;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiry);
    }
}
