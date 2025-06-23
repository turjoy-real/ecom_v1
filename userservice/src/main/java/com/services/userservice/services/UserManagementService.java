package com.services.userservice.services;

import com.services.common.dtos.UserDTO;
import com.services.userservice.dtos.UserMapper;
import com.services.userservice.repositories.UserRepo;

import jakarta.transaction.Transactional;

import com.services.userservice.repositories.AddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserManagementService {
    private final UserRepo userRepo;
    private final AddressRepository addressRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserManagementService.class);

    public UserManagementService(UserRepo userRepo, AddressRepository addressRepository) {
        this.userRepo = userRepo;
        this.addressRepository = addressRepository;
    }

    public boolean userExistsById(Long userId) {
        logger.info("Checking if user exists with ID: {}", userId);
        boolean exists = userRepo.existsById(userId);
        logger.info("User exists: {}", exists);
        return exists;
    }

    public UserDTO getUserDetailsById(Long userId) {
        logger.info("Fetching user details for ID: {}", userId);
        return userRepo.findById(userId)
                .map(user -> {
                    logger.info("User found for ID: {}", userId);
                    return UserMapper.fromEntity(user);
                })
                .orElseGet(() -> {
                    logger.warn("User not found for ID: {}", userId);
                    return null;
                });
    }

    @Transactional
    public boolean deleteUser(Long userId) {
        logger.info("Attempting to delete user with ID: {}", userId);
        if (!userRepo.existsById(userId)) {
            logger.warn("User not found for deletion: {}", userId);
            return false;
        }
        addressRepository.deleteByUserId(userId);
        userRepo.deleteById(userId);
        logger.info("User and related addresses deleted successfully: {}", userId);
        return true;
    }
}