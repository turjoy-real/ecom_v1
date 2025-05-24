package com.services.userservice.services;

import com.services.userservice.models.User;
import com.services.userservice.repositories.UserRepo;
import com.services.userservice.security.models.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepo userRepo;

    public CustomUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username: {}", username);
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> {
                    logger.error("User not found for email: {}", username);
                    return new UsernameNotFoundException("User not found for " + username);
                });
        logger.debug("Found user: {}", user.getEmail());
        return new CustomUserDetails(user);
    }
}