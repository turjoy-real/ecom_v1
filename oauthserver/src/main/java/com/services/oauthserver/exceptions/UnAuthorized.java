package com.services.oauthserver.exceptions;

public class UnAuthorized extends RuntimeException {
    public UnAuthorized(String message) {
        super(message);
    }
}
