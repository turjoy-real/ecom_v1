package com.services.userservice.dtos;

import java.util.List;

import com.services.userservice.models.Role;
import com.services.userservice.models.User;

public class UserDTO {
    private final String email;
    private final String name;
    private final boolean emailVerified;
    private final List<String> roles;

    public UserDTO(String email, String name, boolean emailVerified, List<String> roles) {
        this.email = email;
        this.name = name;
        this.emailVerified = emailVerified;
        this.roles = roles;
    }

    public static UserDTO fromEntity(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        return new UserDTO(
                user.getEmail(),
                user.getName(),
                user.isEmailVerified(),
                roleNames);
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public List<String> getRoles() {
        return roles;
    }
}
