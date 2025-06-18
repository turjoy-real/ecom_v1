package com.services.cartservice.services;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Service
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    public UserServiceClient(
            RestTemplate restTemplate,
            @Value("${user.service.url:http://userservice}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    public boolean verifyUser(String userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId + "/verify";
            HttpHeaders headers = new HttpHeaders();

            // ✅ Forward JWT if present
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                headers.setBearerAuth(jwtAuth.getToken().getTokenValue());
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );

            return response.getStatusCode() == HttpStatus.OK && Boolean.TRUE.equals(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (HttpServerErrorException e) {
            return false;
        } catch (Exception e) {
            // ✅ Optional: log internal error
            log.error("Error verifying user existence for ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Unable to verify user existence. Please try again later.");
        }
    }
}
