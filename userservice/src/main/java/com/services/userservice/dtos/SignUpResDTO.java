package com.services.userservice.dtos;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignUpResDTO {
    private SignUpResponseDto data;
    private HttpStatus status;
}
