package com.services.oauthserver.controllers;

import com.services.oauthserver.dtos.LogoutRequestDto;
import com.services.oauthserver.dtos.SignUpRequestDto;
import com.services.oauthserver.dtos.SignUpResponseDto;
import com.services.oauthserver.models.User;
import com.services.oauthserver.repositories.UserRepo;
import com.services.oauthserver.services.EmailVerificationService;
import com.services.oauthserver.services.PasswordResetService;
import com.services.oauthserver.services.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private UserService userService;
    private EmailVerificationService emailVerificationService;
    private PasswordResetService resetService;
    private UserRepo userRepo;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody SignUpRequestDto requestDto) {

        User user = userService.signUp(requestDto.getName(), requestDto.getEmail(), requestDto.getPassword());
        emailVerificationService.sendVerificationEmail(user);
        SignUpResponseDto signUpResponseDto = toSignUpResponseDto(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(signUpResponseDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequestDto requestDto) {
        logger.info("Token got: " + requestDto.getToken());

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
