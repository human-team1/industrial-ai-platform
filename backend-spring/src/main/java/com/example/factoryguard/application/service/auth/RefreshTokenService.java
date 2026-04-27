package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.dto.auth.RefreshTokenResult;
import com.example.factoryguard.application.port.in.auth.RefreshTokenUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.security.JwtProperties;
import com.example.factoryguard.config.security.JwtTokenProvider;
import com.example.factoryguard.domain.user.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenStorePort tokenStorePort;
    private final JwtProperties jwtProperties;

    @Override
    public RefreshTokenResult execute(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtTokenProvider.extractUserId(refreshToken);
        String role = jwtTokenProvider.extractRole(refreshToken);
        Long organizationId = jwtTokenProvider.extractOrganizationId(refreshToken);

        String stored = tokenStorePort.getRefreshToken(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!stored.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newSessionId = UUID.randomUUID().toString();
        Duration ttl = Duration.ofDays(jwtProperties.getRefreshExpireDays());

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                userId, UserRole.valueOf(role), organizationId, newSessionId);

        tokenStorePort.saveSessionId(userId, newSessionId, ttl);

        return RefreshTokenResult.builder()
                .accessToken(newAccessToken)
                .build();
    }
}