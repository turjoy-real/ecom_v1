package com.services.userservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Setter
@Getter
@Entity
@JsonDeserialize
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User extends BaseModel {

    private String name;
    private String email;
    private String hashedPassword;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles = new ArrayList<>();

    private boolean isEmailVerified;

    public List<Role> getRoles() {
        return new ArrayList<>(roles);
    }
}