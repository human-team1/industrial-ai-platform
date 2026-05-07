package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionValidationService {

    private final TokenStorePort tokenStorePort;
    private final ActiveSessionService activeSessionService;

    public void validate(Long userId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BusinessException(ErrorCode.SESSION_INVALID);
        }
        try {
            if (!tokenStorePort.hasSessionId(userId, sessionId)) {
                throw new BusinessException(ErrorCode.SESSION_INVALID);
            }
            activeSessionService.validateActiveSession(sessionId);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("Session store validation failed, userId={}, sessionIdPrefix={}, reason={}",
                    userId, maskSessionId(sessionId), exception.getMessage(), exception);
            throw new BusinessException(ErrorCode.SESSION_STORE_UNAVAILABLE);
        }
    }

    private String maskSessionId(String sessionId) {
        if (sessionId == null || sessionId.length() < 8) {
            return "n/a";
        }
        return sessionId.substring(0, 8);
    }
}
