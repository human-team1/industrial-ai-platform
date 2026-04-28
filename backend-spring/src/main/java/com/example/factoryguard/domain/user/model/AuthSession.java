package com.example.factoryguard.domain.user.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuthSession {

    private final Long authSessionId;
    private final Long userId;
    private final String accessToken;
    private final String refreshToken;
    private final String ipAddress;
    private final String userAgent;
    private final LocalDateTime loginAt;
    private final LocalDateTime expiresAt;
    private final LocalDateTime revokedAt;
}
