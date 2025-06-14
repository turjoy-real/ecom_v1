package com.services.oauthserver.dtos;

import com.services.oauthserver.models.ErrorResponse;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignUpResponseDto {
    private String name;
    private String email;
    private boolean isEmailVerified;
}
