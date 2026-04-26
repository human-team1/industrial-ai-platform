package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.dto.auth.RefreshTokenResult;
import com.example.factoryguard.application.port.in.auth.RefreshTokenUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.security.JwtTokenProvider;
import com.example.factoryguard.domain.user.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenStorePort tokenStorePort;

    @Override
    public RefreshTokenResult execute(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtTokenProvider.extractUserId(refreshToken);
        String role = jwtTokenProvider.extractRole(refreshToken);

        String stored = tokenStorePort.getRefreshToken(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!stored.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, UserRole.valueOf(role));

        return RefreshTokenResult.builder()
                .accessToken(newAccessToken)
                .build();
    }
}
