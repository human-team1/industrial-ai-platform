package com.example.factoryguard.application.port.in.dto;

import com.example.factoryguard.domain.user.UserStatus;

public class PendingUserResult {
    private final Long userId;
    private final String email;
    private final String name;
    private final UserStatus status;
    private final String createdAt;

    public PendingUserResult(Long userId, String email, String name, UserStatus status, String createdAt) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getCreatedAt() {
        return createdAt;
    }
}