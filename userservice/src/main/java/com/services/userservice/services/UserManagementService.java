package com.services.userservice.services;

import com.services.userservice.repositories.UserRepo;
import org.springframework.stereotype.Service;

@Service
public class UserManagementService {
    private final UserRepo userRepo;

    public UserManagementService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public boolean userExistsById(Long userId) {
        return userRepo.existsById(userId);
    }
} 