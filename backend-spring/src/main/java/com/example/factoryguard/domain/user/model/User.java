package com.example.factoryguard.domain.user.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class User {

    private final Long userId;
    private final Long organizationId;
    private final String googleSub;
    private final String email;
    private final String passwordHash;
    private final String name;
    private final String picture;
    private final String phone;
    private final UserStatus status;
    private final UserRole role;
    private final LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;
}
