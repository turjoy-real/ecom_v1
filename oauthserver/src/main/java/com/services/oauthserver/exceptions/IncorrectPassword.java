package com.services.oauthserver.exceptions;

public class IncorrectPassword extends RuntimeException {
    public IncorrectPassword(String message) {
        super(message);
    }

}
