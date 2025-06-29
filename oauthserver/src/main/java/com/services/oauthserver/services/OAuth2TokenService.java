package com.services.oauthserver.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.services.oauthserver.models.User;
import com.services.oauthserver.security.KeyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OAuth2TokenService {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2TokenService.class);
    private static final String ISSUER = "http://localhost:9001";
    private static final String AUDIENCE = "spa-client";
    private static final String KEY_ID = "your-key-id";
    private static final long TOKEN_EXPIRY_MINUTES = 30;
    
    private final RSAPrivateKey privateKey;

    public OAuth2TokenService() throws Exception {
        try {
            this.privateKey = (RSAPrivateKey) KeyLoader.loadPrivateKey("private.pem");
            logger.info("OAuth2TokenService initialized successfully with RSA private key");
        } catch (Exception e) {
            logger.error("Failed to initialize OAuth2TokenService: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String generateAccessToken(User user) throws JOSEException {
        try {
            // Create JWT claims set
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getId().toString())
                    .issuer(ISSUER)
                    .audience(AUDIENCE)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + TOKEN_EXPIRY_MINUTES * 60 * 1000)) // 30 minutes
                    .jwtID(UUID.randomUUID().toString())
                    .claim("user_email", user.getEmail())
                    .claim("roles", user.getRoles().stream().map(role -> role.getName()).toList())
                    .build();

            // Create signed JWT
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(KEY_ID)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(new RSASSASigner(privateKey));

            logger.debug("Generated OAuth2 access token for user: {}", user.getEmail());
            return signedJWT.serialize();
        } catch (JOSEException e) {
            logger.error("Failed to generate OAuth2 access token for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
} 