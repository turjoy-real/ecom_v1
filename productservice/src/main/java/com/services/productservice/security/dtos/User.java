package com.services.productservice.security.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class User {
    private String name;
    private String email;
    private String hashedPassword;
    private List<Role> roles;
    private boolean isEmailVerified;
}
