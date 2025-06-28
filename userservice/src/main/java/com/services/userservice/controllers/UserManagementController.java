package com.services.userservice.controllers;

import com.services.common.dtos.UserDTO;
import com.services.userservice.services.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/userdata")
public class UserManagementController {
    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/verify")
    public ResponseEntity<Boolean> verifyUser(Authentication authentication) {
        boolean exists = userManagementService.userExistsById(Long.parseLong(authentication.getName()));
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.name")
    public ResponseEntity<UserDTO> getUserDetails(@PathVariable Long userId) {
        UserDTO userDetails = userManagementService.getUserDetailsById(userId);
        if (userDetails == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDetails);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        boolean deleted = userManagementService.deleteUser(userId);
        if (deleted) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}