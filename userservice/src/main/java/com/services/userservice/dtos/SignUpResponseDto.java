package com.services.userservice.dtos;


import com.services.userservice.models.ErrorResponse;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignUpResponseDto {
    private String name;
    private String email;
    private boolean isEmailVerified;
}
