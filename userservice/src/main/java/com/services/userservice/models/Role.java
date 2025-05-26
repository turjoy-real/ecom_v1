package com.services.userservice.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@JsonDeserialize
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Role extends BaseModel {
    private String name;
}
