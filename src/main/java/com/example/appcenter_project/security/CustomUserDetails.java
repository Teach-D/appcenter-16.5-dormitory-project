package com.example.appcenter_project.security;

import com.example.appcenter_project.entity.user.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    @JsonIgnore
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // Jackson을 위한 기본 생성자 추가
    @JsonCreator
    public CustomUserDetails() {
        this.user = null;
    }

    @Override
    @JsonProperty("username")
    public String getUsername() {
        return user != null ? user.getStudentNumber() : null;
    }

    @JsonProperty("id")
    public Long getId() {
        return user != null ? user.getId() : null;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return user != null ? user.getPassword() : null;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user != null ? List.of(new SimpleGrantedAuthority(user.getRole().name())) : List.of();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() { return true; }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() { return true; }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    @JsonIgnore
    public boolean isEnabled() { return true; }

    @JsonIgnore
    public User getUser() {
        return user;
    }
}