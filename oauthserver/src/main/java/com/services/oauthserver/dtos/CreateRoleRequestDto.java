package com.services.oauthserver.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoleRequestDto {
    @NotBlank(message = "Role name is required")
    private String name;
}