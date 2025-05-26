package com.services.userservice.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// PasswordResetEvent.java
public class PasswordResetEvent {
    private String to;
    private String subject;
    private String resetLink;
}