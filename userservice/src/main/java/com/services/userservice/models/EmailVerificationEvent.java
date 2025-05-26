package com.services.userservice.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
// EmailVerificationEvent.java
public class EmailVerificationEvent {
    private String to;
    private String subject;
    private String verificationLink;
}