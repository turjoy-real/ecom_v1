package com.services.userservice.services;

import com.services.common.dtos.UserDTO;
import com.services.userservice.dtos.UserMapper;
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

    public UserDTO getUserDetailsById(Long userId) {
        return userRepo.findById(userId)
                .map(UserMapper::fromEntity)
                .orElse(null);
    }
}