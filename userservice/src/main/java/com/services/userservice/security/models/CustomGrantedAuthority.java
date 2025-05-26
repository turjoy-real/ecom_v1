package com.services.userservice.security.models;

import com.services.userservice.models.Role;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
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