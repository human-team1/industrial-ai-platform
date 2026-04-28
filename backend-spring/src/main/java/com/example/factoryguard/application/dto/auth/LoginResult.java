package com.example.factoryguard.application.dto.auth;

import com.example.factoryguard.domain.user.model.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResult {

    private final Long userId;
    private final String email;
    private final String name;
    private final UserRole role;
    private final String accessToken;
    private final String refreshToken;
    private final String sessionId;
}
