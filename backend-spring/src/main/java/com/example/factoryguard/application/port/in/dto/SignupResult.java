package com.example.factoryguard.application.port.in.dto;

import com.example.factoryguard.domain.user.UserStatus;

public class SignupResult {
    private final Long userId;
    private final String email;
    private final String name;
    private final UserStatus status;

    public SignupResult(Long userId, String email, String name, UserStatus status) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public UserStatus getStatus() {
        return status;
    }
}