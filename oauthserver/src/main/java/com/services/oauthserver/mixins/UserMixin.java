package com.services.oauthserver.mixins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.services.oauthserver.models.Role;

import java.util.List;

public abstract class UserMixin {
    @JsonCreator
    public UserMixin(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("hashedPassword") String hashedPassword,
            @JsonProperty("roles") List<Role> roles,
            @JsonProperty("isEmailVerified") boolean isEmailVerified) {
    }
}