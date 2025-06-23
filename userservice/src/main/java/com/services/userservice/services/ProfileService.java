package com.services.userservice.services;

import com.services.common.dtos.UserDTO;
import com.services.userservice.dtos.ProfileUpdateRequest;
import com.services.userservice.dtos.UserMapper;
import com.services.userservice.exceptions.UserNotFound;
import com.services.userservice.models.Role;
import com.services.userservice.models.User;
import com.services.userservice.repositories.UserRepo;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    private final UserRepo userRepository;

    public ProfileService(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO getUserProfile(String id) {
        User user = userRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new UserNotFound("User not found with id: " + id));
        return UserMapper.fromEntity(user);
    }

    @Transactional
    public UserDTO updateUserProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + email));

        user.setName(request.getName());
        user = userRepository.save(user);
        return UserMapper.fromEntity(user);
    }

    public UserDTO getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + email));
        return UserMapper.fromEntity(user);
    }

    boolean userExistsById(Long userId) {
        return userRepository.existsById(userId);
    }
}