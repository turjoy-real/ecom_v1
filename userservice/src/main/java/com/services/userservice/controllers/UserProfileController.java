package com.services.userservice.controllers;

import com.services.common.dtos.UserDTO;
import com.services.userservice.dtos.ProfileUpdateRequest;
import com.services.userservice.services.ProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/profile")
@Validated
public class UserProfileController {
    private final ProfileService profileService;

    public UserProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<UserDTO> getMyProfile(Authentication authentication) {
        String id = authentication.getName();
        return ResponseEntity.ok(profileService.getUserProfile(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserDTO> getUserProfileById(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(profileService.getUserProfile(userId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-token")
    public ResponseEntity<UserDTO> getMyProfileByToken(@RequestHeader("Authorization") String token) {
        // Extract user ID from JWT token
        try {
            // Remove "Bearer " prefix if present
            String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            
            // Decode JWT to get user ID
            String userId = extractUserIdFromToken(jwtToken);
            return ResponseEntity.ok(profileService.getUserProfile(userId));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    private String extractUserIdFromToken(String token) {
        // This is a simplified JWT parsing - in production, you should use proper JWT library
        // For now, we'll use Spring Security's JWT decoder
        try {
            // You might need to inject JwtDecoder here
            // For now, let's assume we can extract the subject claim
            // This is a placeholder implementation
            return "1"; // This should be replaced with actual JWT parsing
        } catch (Exception e) {
            throw new RuntimeException("Invalid token", e);
        }
    }

    @PatchMapping
    public ResponseEntity<UserDTO> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProfileUpdateRequest request) {
        String email = jwt.getClaim("user_email");
        return ResponseEntity.ok(profileService.updateUserProfile(email, request));
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getProfileByEmail(@Email @PathVariable String email) {
        return ResponseEntity.ok(profileService.getProfileByEmail(email));
    }

    @PatchMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateProfileByEmail(
            @Email @PathVariable String email,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateUserProfile(email, request));
    }

    public String getUserEmailFromJwt() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("user_email");
    }
}