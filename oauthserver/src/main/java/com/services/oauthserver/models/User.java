package com.services.oauthserver.models;

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
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" }) // Required to ignore Hibernate proxies
public class User extends BaseModel {

    private String name;
    private String email;
    private String hashedPassword;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles = new ArrayList<>(); // Prevents PersistentBag on serialization

    private boolean isEmailVerified;

    // Force returning a safe list for Jackson
    public List<Role> getRoles() {
        return new ArrayList<>(roles);
    }
}