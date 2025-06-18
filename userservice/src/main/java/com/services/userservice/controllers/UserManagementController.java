package com.services.userservice.controllers;

import com.services.userservice.services.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {
    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/{userId}/verify")
    public ResponseEntity<Boolean> verifyUser(@PathVariable Long userId) {
        boolean exists = userManagementService.userExistsById(userId);
        return ResponseEntity.ok(exists);
    }
} 