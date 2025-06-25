package com.services.userservice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRoleRequest {
    @NotBlank(message = "User email is required")
    @Email
    private String userEmail;

    @NotBlank(message = "Role name is required")
    private String roleName;
}