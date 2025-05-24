package com.services.userservice.mixins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class RoleMixin {
    @JsonCreator
    public RoleMixin(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name) {
    }
}