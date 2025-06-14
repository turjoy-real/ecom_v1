package com.services.oauthserver.security.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.services.oauthserver.models.Role;
import com.services.oauthserver.models.User;

import lombok.NoArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
// @NoArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final User user;
    private final List<CustomGrantedAuthority> authorities;

    @JsonCreator
    public CustomUserDetails(
            @JsonProperty("user") User user,
            @JsonProperty("authorities") List<CustomGrantedAuthority> authorities) {
        this.user = user;
        // this.authorities = authorities;
        // this.authorities = user.getRoles()
        // .stream()
        // .map(role -> new CustomGrantedAuthority(role.getName()))
        // .collect(Collectors.toList());
        // this.authorities = new ArrayList<>(user.getRoles())
        // .stream()
        // .map(role -> new CustomGrantedAuthority(role.getName()))
        // .collect(Collectors.toList());
        this.authorities = user.getRoles().stream()
                .map(role -> new CustomGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    public CustomUserDetails(User user) {
        this.user = user;
        this.authorities = user.getRoles().stream()
                .map(CustomGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    // Accessors for custom fields
    public String getName() {
        return user.getName();
    }

    public boolean isEmailVerified() {
        return user.isEmailVerified();
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities != null) {
            return authorities;
        }
        List<CustomGrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            grantedAuthorities.add(new CustomGrantedAuthority(role));
        }
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return user.getHashedPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}