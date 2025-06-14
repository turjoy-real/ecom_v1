package com.services.oauthserver.mixins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.services.oauthserver.models.User;
import com.services.oauthserver.security.models.CustomGrantedAuthority;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class CustomUserDetailsMixin {
    @JsonCreator
    public CustomUserDetailsMixin(
            @JsonProperty("user") User user,
            @JsonProperty("authorities") List<CustomGrantedAuthority> authorities) {
    }
}