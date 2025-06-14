package com.services.oauthserver.exceptions;

public class TokenNotFound extends RuntimeException {
    public TokenNotFound(String message) {
        super(message);
    }
}
