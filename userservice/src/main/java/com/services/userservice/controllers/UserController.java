package com.services.userservice.controllers;

import com.services.userservice.dtos.LogoutRequestDto;
import com.services.userservice.dtos.ResendVerificationEmailRequestDto;
import com.services.userservice.dtos.SignUpRequestDto;
import com.services.userservice.dtos.SignUpResponseDto;
import com.services.userservice.models.User;
import com.services.userservice.repositories.UserRepo;
import com.services.userservice.services.EmailVerificationService;
import com.services.userservice.services.PasswordResetService;
import com.services.userservice.services.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private UserService userService;
    private EmailVerificationService emailVerificationService;
    private PasswordResetService resetService;
    private UserRepo userRepo;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody SignUpRequestDto requestDto) {
        SignUpResponseDto signUpResponseDto = toSignUpResponseDto(
                userService.signUp(requestDto.getName(), requestDto.getEmail(), requestDto.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(signUpResponseDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequestDto requestDto) {
        // delete token if exists -> 200
        // if doesn't exist give a 404

        userService.logout(requestDto.getToken());
        return ResponseEntity.ok().build(); // or throw an exception, based on your error handling policy
    }

    public SignUpResponseDto toSignUpResponseDto(User user) {
        if (user == null) {
            return null; // Or throw an exception, based on your error handling policy
        }

        SignUpResponseDto dto = new SignUpResponseDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setEmailVerified(user.isEmailVerified());

        return dto;
    }

    @PostMapping("/validate/{token}")
    public User validateToken(@PathVariable("token") @NonNull String token) {
        return userService.validateToken(token);
    }

    @PostMapping("/findByEmail/{email}")
    public ResponseEntity<User> findByEmail(@PathVariable("email") @NonNull String email) {
        User user = userService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/verify/{userId}")
    public ResponseEntity<Boolean> verifyUser(@PathVariable("userId") Long userId) {
        boolean exists = userService.verifyUser(userId);
        return ResponseEntity.ok(exists);
    }

    // POST /api/request-reset?email=someone@example.com
    @PostMapping("/open/request-reset")
    public ResponseEntity<String> requestReset(@RequestParam String email) {
        resetService.requestPasswordReset(email);
        return ResponseEntity.ok("Email Sent.");
    }

    // POST /api/reset-password
    @PostMapping("/open/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token,
            @RequestParam String newPassword) {
        boolean ok = resetService.resetPassword(token, newPassword);
        return ok ? ResponseEntity.ok("Password reset successful")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expiredtoken");
    }

    @Getter
    public static class PasswordResetRequest {
        private String token;
        private String newPassword;
    }

    @PostMapping("/open/send-verification")
    public ResponseEntity<String> sendVerification(@RequestParam String email) {
        userRepo.findByEmail(email).ifPresent(emailVerificationService::sendVerificationEmail);
        return ResponseEntity.ok("If your email exists in our system, a verification link has been sent.");
    }

    // GET /api/verify-email?token=abc
    @GetMapping("/open/verify-email")
    public RedirectView verifyEmail(@RequestParam String token) {
        boolean ok = emailVerificationService.verify(token);
        return new RedirectView(ok ? "/html/email-verified.html" : "/html/verification-failed.html");
    }
}
