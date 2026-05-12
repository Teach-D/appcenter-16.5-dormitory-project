package com.example.appcenter_project.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "test-account")
public class TestAccountProperties {

    private String studentNumber;
    private String password;
    private String name;
    private String dormType;
    private String college;
    private String role;

    public boolean matches(String inputId, String inputPassword) {
        return isConfigured()
                && studentNumber.equals(inputId)
                && password.equals(inputPassword);
    }

    private boolean isConfigured() {
        return studentNumber != null && !studentNumber.isBlank();
    }
}
