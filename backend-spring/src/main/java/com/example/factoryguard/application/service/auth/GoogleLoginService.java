package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.dto.auth.GoogleLoginCommand;
import com.example.factoryguard.application.dto.auth.GoogleLoginResult;
import com.example.factoryguard.application.dto.auth.GoogleTokenInfo;
import com.example.factoryguard.application.dto.operation.RecordOperationLogCommand;
import com.example.factoryguard.application.port.in.auth.GoogleLoginUseCase;
import com.example.factoryguard.application.port.in.operation.RecordOperationLogUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.auth.VerifyGoogleTokenPort;
import com.example.factoryguard.application.port.out.user.FindUserByGoogleSubPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.security.JwtProperties;
import com.example.factoryguard.config.security.JwtTokenProvider;
import com.example.factoryguard.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GoogleLoginService implements GoogleLoginUseCase {

    private final VerifyGoogleTokenPort verifyGoogleTokenPort;
    private final FindUserByGoogleSubPort findUserByGoogleSubPort;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenStorePort tokenStorePort;
    private final JwtProperties jwtProperties;
    private final RecordOperationLogUseCase recordOperationLogUseCase;
    private final ActiveSessionService activeSessionService;

    @Override
    public GoogleLoginResult execute(GoogleLoginCommand command) {
        GoogleTokenInfo tokenInfo;
        try {
            tokenInfo = verifyGoogleTokenPort.verify(command.getIdToken());
        } catch (RuntimeException exception) {
            recordOperationLogUseCase.recordOperationLog(RecordOperationLogCommand.builder()
                    .eventType("LOGIN_FAILED")
                    .eventStatus("FAILED")
                    .logLevel("WARN")
                    .sourceComponent("SPRING_API")
                    .detailMessage("Google 로그인 토큰 검증 실패")
                    .relatedPath("/api/v1/auth/google-login")
                    .build());
            throw exception;
        }

        Optional<User> userOpt = findUserByGoogleSubPort.findByGoogleSub(tokenInfo.getSub());

        if (userOpt.isEmpty()) {
            String signupToken = jwtTokenProvider.generateSignupToken(
                    tokenInfo.getSub(),
                    tokenInfo.getEmail(),
                    tokenInfo.getName(),
                    tokenInfo.getPicture());
            return GoogleLoginResult.ofNew(tokenInfo, signupToken);
        }

        User user = userOpt.get();

        return switch (user.getStatus()) {
            case ACTIVE -> {
                String sessionId = UUID.randomUUID().toString();
                Duration ttl = Duration.ofDays(jwtProperties.getRefreshExpireDays());
                activeSessionService.assertCanRegisterNewSession();

                String accessToken = jwtTokenProvider.generateAccessToken(
                        user.getUserId(), user.getRole(), user.getOrganizationId(), sessionId);
                String refreshToken = jwtTokenProvider.generateRefreshToken(
                        user.getUserId(), user.getRole(), user.getOrganizationId(), sessionId);

                try {
                    tokenStorePort.saveRefreshToken(user.getUserId(), sessionId, refreshToken, ttl);
                    tokenStorePort.saveSessionId(user.getUserId(), sessionId, ttl);
                    activeSessionService.registerSession(user.getUserId(), sessionId);
                } catch (RuntimeException exception) {
                    tokenStorePort.deleteRefreshToken(user.getUserId(), sessionId);
                    tokenStorePort.deleteSessionId(user.getUserId(), sessionId);
                    activeSessionService.removeSession(sessionId);
                    throw exception;
                }
                recordOperationLogUseCase.recordOperationLog(RecordOperationLogCommand.builder()
                        .eventType("LOGIN_SUCCESS")
                        .eventStatus("SUCCESS")
                        .logLevel("INFO")
                        .sourceComponent("SPRING_API")
                        .actorUserId(user.getUserId())
                        .detailMessage("사용자 로그인이 완료되었습니다.")
                        .relatedPath("/api/v1/auth/google-login")
                        .build());

                yield GoogleLoginResult.ofActive(
                        accessToken, refreshToken,
                        user.getUserId(), user.getGoogleSub(),
                        user.getEmail(), user.getName(),
                        user.getPicture(), user.getRole().name(),
                        user.getOrganizationId()
                );
            }
            case PENDING -> GoogleLoginResult.ofPending(user.getEmail(), user.getName());
            case REJECTED -> GoogleLoginResult.ofRejected(user.getEmail(), user.getName());
            case INACTIVE -> {
                recordOperationLogUseCase.recordOperationLog(RecordOperationLogCommand.builder()
                        .eventType("LOGIN_FAILED")
                        .eventStatus("FAILED")
                        .logLevel("WARN")
                        .sourceComponent("SPRING_API")
                        .actorUserId(user.getUserId())
                        .detailMessage("비활성화된 계정의 로그인 시도")
                        .relatedPath("/api/v1/auth/google-login")
                        .build());
                throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
            }
        };
    }
}
