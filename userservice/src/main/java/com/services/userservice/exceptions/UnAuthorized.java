package com.services.userservice.exceptions;

public class UnAuthorized extends RuntimeException {
    public UnAuthorized(String message) {
        super(message);
    }
}
