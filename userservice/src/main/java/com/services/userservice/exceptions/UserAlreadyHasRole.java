package com.services.userservice.exceptions;

public class UserAlreadyHasRole extends RuntimeException {
    public UserAlreadyHasRole(String msg) {
        super(msg);
    }
}
