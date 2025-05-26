package com.services.userservice.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ErrorResponse {
    private String error;
    private String message;
    private int statusCode;
}