package com.services.userservice.security.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.services.userservice.models.Role;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;

public class CustomGrantedAuthority implements GrantedAuthority {
    private final String authority;

    @JsonCreator
    public CustomGrantedAuthority(@JsonProperty("authority") String authority) {
        this.authority = authority;
    }

    public CustomGrantedAuthority(Role role) {
        this.authority = role.getName();
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}