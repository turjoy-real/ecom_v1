package com.services.paymentservice.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Base64;

@Service
@Slf4j
public class OAuthClientCredentialsService implements InitializingBean {
    private static final String TOKEN_URL = "http://localhost:9001/oauth2/token";
    private static final String CLIENT_ID = "paymentservice";
    private static final String CLIENT_SECRET = "change-this-to-a-strong-secret";
    private static final String SCOPE = "internal.webhook";

    private final AtomicReference<String> token = new AtomicReference<>();
    private final AtomicReference<Long> expiry = new AtomicReference<>(0L);
    private final RestTemplate restTemplate = new RestTemplate();

    public String getToken() {
        if (System.currentTimeMillis() > expiry.get()) {
            fetchToken();
        }
        return token.get();
    }

    @Override
    public void afterPropertiesSet() {
        fetchToken();
    }

    @Scheduled(fixedDelay = 300000) // every 5 minutes
    public void refreshToken() {
        fetchToken();
    }

    private synchronized void fetchToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            // Add HTTP Basic Auth header
            String creds = CLIENT_ID + ":" + CLIENT_SECRET;
            String base64Creds = Base64.getEncoder().encodeToString(creds.getBytes());
            headers.set("Authorization", "Basic " + base64Creds);
            // Only grant_type and scope in body
            String body = String.format(
                "grant_type=client_credentials&scope=%s",
                SCOPE
            );
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                TOKEN_URL, HttpMethod.POST, request, Map.class
            );
            Map<String, Object> resp = response.getBody();
            if (resp != null && resp.containsKey("access_token")) {
                token.set("Bearer " + resp.get("access_token"));
                int expiresIn = ((Number) resp.getOrDefault("expires_in", 3600)).intValue();
                expiry.set(System.currentTimeMillis() + (expiresIn - 60) * 1000L); // refresh 1 min early
                log.info("Obtained client credentials token from oauthserver");
            } else {
                log.error("Failed to obtain access token: {}", resp);
            }
        } catch (Exception e) {
            log.error("Error fetching client credentials token", e);
        }
    }
} 