package com.services.userservice.services;

import com.services.userservice.dtos.ProfileUpdateRequest;
import com.services.userservice.dtos.UserDTO;
import com.services.userservice.exceptions.UserNotFound;
import com.services.userservice.models.User;
import com.services.userservice.repositories.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    private final UserRepo userRepository;

    public ProfileService(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + email));
        return UserDTO.fromEntity(user);
    }

    @Transactional
    public UserDTO updateUserProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + email));

        user.setName(request.getName());
        user = userRepository.save(user);
        return UserDTO.fromEntity(user);
    }

    public UserDTO getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + email));
        return UserDTO.fromEntity(user);
    }

    boolean userExistsById(Long userId) {
        return userRepository.existsById(userId);
    }
}