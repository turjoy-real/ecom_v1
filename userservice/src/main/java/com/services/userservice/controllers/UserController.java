package com.services.userservice.controllers;

import com.services.userservice.dtos.LoginRequestDto;
import com.services.userservice.dtos.LoginResponseDto;
import com.services.userservice.dtos.LogoutRequestDto;
import com.services.userservice.dtos.SignUpRequestDto;
import com.services.userservice.dtos.SignUpResponseDto;
import com.services.userservice.mappers.ResponseMappers;
import com.services.userservice.models.Token;
import com.services.userservice.models.User;
import com.services.userservice.services.UserService;

import jakarta.validation.Valid;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // @PostMapping("/login")
    // public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody
    // LoginRequestDto requestDto) {
    // // check if email and password in db
    // // if yes create token (use random string) return token
    // // else throw some error
    // return ResponseEntity.ok()
    // .body(ResponseMappers
    // .toLoginResponseDto(userService.login(requestDto.getEmail(),
    // requestDto.getPassword())));
    // }

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
}
