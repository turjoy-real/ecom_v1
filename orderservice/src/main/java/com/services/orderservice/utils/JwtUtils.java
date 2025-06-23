package com.services.orderservice.utils;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    private final JwtDecoder jwtDecoder;

    public JwtUtils(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public String extractUserIdFromToken(String token) {
        try {
            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Jwt jwt = jwtDecoder.decode(token);

            // Try to get user_id claim first, then fall back to sub
            String userId = jwt.getClaimAsString("user_id");
            if (userId == null || userId.isEmpty()) {
                userId = jwt.getSubject(); // sub claim
            }

            if (userId == null || userId.isEmpty()) {
                throw new RuntimeException("No user ID found in token");
            }

            return userId;
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error processing JWT token: " + e.getMessage());
        }
    }
}