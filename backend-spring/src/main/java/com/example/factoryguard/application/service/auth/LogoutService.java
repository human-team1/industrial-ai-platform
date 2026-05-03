package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.port.in.auth.LogoutUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final TokenStorePort tokenStorePort;
    private final ActiveSessionService activeSessionService;

    @Override
    public void execute(Long userId, String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            tokenStorePort.deleteRefreshToken(userId, sessionId);
            tokenStorePort.deleteSessionId(userId, sessionId);
            activeSessionService.removeSession(sessionId);
            return;
        }
        tokenStorePort.deleteSessionId(userId);
    }
}
