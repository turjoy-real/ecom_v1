package com.services.userservice.models;

public enum UserRole {
    ROLE_USER("Basic user access"),
    ROLE_ADMIN("Administrative access"),
    ROLE_MANAGER("Managerial access");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}