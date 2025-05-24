package com.services.userservice.mixins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.services.userservice.models.User;
import com.services.userservice.security.models.CustomGrantedAuthority;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class CustomUserDetailsMixin {
    @JsonCreator
    public CustomUserDetailsMixin(
            @JsonProperty("user") User user,
            @JsonProperty("authorities") List<CustomGrantedAuthority> authorities) {
    }
}