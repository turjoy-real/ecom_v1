package com.services.oauthserver.exceptions;

public class UserAlreadyRegistered extends RuntimeException {
    public UserAlreadyRegistered(String msg) {
        super(msg);
    }
}
