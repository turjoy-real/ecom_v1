package com.services.userservice.exceptions;

public class UserNotFound  extends RuntimeException {
    public UserNotFound(String message) {
        super(message);
    }
}
