package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.dto.auth.GoogleLoginCommand;
import com.example.factoryguard.application.dto.auth.GoogleLoginResult;
import com.example.factoryguard.application.dto.auth.GoogleTokenInfo;
import com.example.factoryguard.application.port.in.auth.GoogleLoginUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.auth.VerifyGoogleTokenPort;
import com.example.factoryguard.application.port.out.user.FindUserByGoogleSubPort;
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

    @Override
    public GoogleLoginResult execute(GoogleLoginCommand command) {
        GoogleTokenInfo tokenInfo = verifyGoogleTokenPort.verify(command.getIdToken());

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

                String accessToken = jwtTokenProvider.generateAccessToken(
                        user.getUserId(), user.getRole(), user.getOrganizationId(), sessionId);
                String refreshToken = jwtTokenProvider.generateRefreshToken(
                        user.getUserId(), user.getRole(), user.getOrganizationId(), sessionId);

                tokenStorePort.saveRefreshToken(user.getUserId(), refreshToken, ttl);
                tokenStorePort.saveSessionId(user.getUserId(), sessionId, ttl);

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
            default -> GoogleLoginResult.ofPending(user.getEmail(), user.getName());
        };
    }
}