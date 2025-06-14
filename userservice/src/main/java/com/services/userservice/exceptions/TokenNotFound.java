package com.services.userservice.exceptions;

public class TokenNotFound extends RuntimeException {
    public TokenNotFound(String message) {
        super(message);
    }
}
