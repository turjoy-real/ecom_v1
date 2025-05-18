package com.services.productservice.security.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.services.productservice.security.dtos.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RestTemplate restTemplate;
    private String userServiceUrl = "http://localhost:9000/users/validate/"; // Replace with your user service URL

    public boolean authenticate(String token) {
        ResponseEntity<User> userResEntity = restTemplate.postForEntity(userServiceUrl + token, token, User.class);

        if (userResEntity.getBody() != null) {
            return true;
            // User user = userResEntity.getBody();
            // return user.getEmail() != null && user.getName() != null;
        } else {
            return false;
        }
    }
}
