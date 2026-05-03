package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionValidationService {

    private final TokenStorePort tokenStorePort;
    private final ActiveSessionService activeSessionService;

    public void validate(Long userId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BusinessException(ErrorCode.SESSION_INVALID);
        }
        if (!tokenStorePort.hasSessionId(userId, sessionId)) {
            throw new BusinessException(ErrorCode.SESSION_INVALID);
        }
        activeSessionService.validateActiveSession(sessionId);
    }
}
