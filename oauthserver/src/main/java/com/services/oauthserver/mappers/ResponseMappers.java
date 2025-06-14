package com.services.oauthserver.mappers;

import com.services.oauthserver.dtos.LoginResponseDto;
import com.services.oauthserver.dtos.SignUpResponseDto;
import com.services.oauthserver.models.Token;
import com.services.oauthserver.models.User;

public class ResponseMappers {
    public static SignUpResponseDto toSignUpResponseDto(User user) {
        if (user == null) {
            return null; // Or throw an exception, based on your error handling policy
        }

        SignUpResponseDto dto = new SignUpResponseDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setEmailVerified(user.isEmailVerified());

        return dto;
    }

    public static LoginResponseDto toLoginResponseDto(Token token) {
        if (token == null) {
            return null; // Or throw an exception, based on your error handling policy
        }
        User user = token.getUser();
        if (user == null) {
            return null; // Or throw an exception, based on your error handling policy
        }

        LoginResponseDto dto = new LoginResponseDto();
        dto.setAccessToken(token.getValue());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setExpiresIn(token.getExpiryAt());

        return dto;
    }
}
