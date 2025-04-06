package com.services.userservice.dtos;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String scope;
    private Date expiresIn;
    private String email;
    private String name;
    private boolean emailVerified;
}