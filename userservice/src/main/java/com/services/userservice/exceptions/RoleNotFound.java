package com.services.userservice.exceptions;

public class RoleNotFound extends RuntimeException {
    public RoleNotFound(String message) {
        super(message);
    }

}
