package com.example.appcenter_project.jwt;

import com.example.appcenter_project.entity.user.User;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Getter
public class SecurityUser extends org.springframework.security.core.userdetails.User {

    private final User user;
    private final Long id;

    public SecurityUser(User user) {
        super(user.getStudentNumber(), "1234",
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())));
        this.user = user;
        this.id = user.getId();
    }
}