package com.services.oauthserver.security;

import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyLoader {
    public static PrivateKey loadPrivateKey(String filename) throws Exception {
        ClassPathResource resource = new ClassPathResource(filename);
        if (!resource.exists()) {
            throw new IllegalStateException("Private key file not found: " + filename);
        }
        String key = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            .replaceAll("-----\\w+ PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public static PublicKey loadPublicKey(String filename) throws Exception {
        ClassPathResource resource = new ClassPathResource(filename);
        if (!resource.exists()) {
            throw new IllegalStateException("Public key file not found: " + filename);
        }
        String key = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            .replaceAll("-----\\w+ PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}