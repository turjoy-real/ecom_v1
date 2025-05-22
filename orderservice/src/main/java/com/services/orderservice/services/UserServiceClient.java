package com.services.orderservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import com.services.orderservice.exceptions.UserVerificationException;

@Service
public class UserServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate,
            @Value("${user.service.url:http://userservice}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
        logger.info("UserServiceClient initialized with URL: {}", userServiceUrl);
    }

    public boolean verifyUser(String userId) {
        String url = userServiceUrl + "/api/users/verify/" + userId;
        logger.info("Verifying user with ID: {} at URL: {}", userId, url);

        try {
            ResponseEntity<Boolean> response = restTemplate.getForEntity(
                    url,
                    Boolean.class,
                    userId);
            boolean exists = Boolean.TRUE.equals(response.getBody());
            logger.info("User verification response for userId {}: {}", userId, exists);
            return exists;
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("User not found with ID: {}", userId);
            return false;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error verifying user {}: {} - {}", userId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new UserVerificationException(
                    "Failed to verify user: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Unexpected error verifying user {}: {}", userId, e.getMessage(), e);
            throw new UserVerificationException("Failed to verify user: " + e.getMessage(), e);
        }
    }
}