package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.port.out.auth.ActiveSessionStorePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.auth.ActiveSessionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActiveSessionService {

    private final ActiveSessionStorePort activeSessionStorePort;
    private final ActiveSessionProperties properties;

    public void assertCanRegisterNewSession() {
        cleanupStaleSessions();
        if (countActiveSessions() >= properties.getMaxActiveUsers()) {
            throw new BusinessException(ErrorCode.ACTIVE_USER_LIMIT_EXCEEDED);
        }
    }

    public void registerSession(Long userId, String sessionId) {
        Duration ttl = ttl();
        activeSessionStorePort.save(sessionId, userId == null ? "anonymous" : String.valueOf(userId), ttl);
        activeSessionStorePort.addToIndex(sessionId);
    }

    public void removeSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        activeSessionStorePort.delete(sessionId);
        activeSessionStorePort.removeFromIndex(sessionId);
    }

    public boolean isActive(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }
        return activeSessionStorePort.exists(sessionId);
    }

    public void refreshSession(String sessionId) {
        if (!isActive(sessionId)) {
            return;
        }
        activeSessionStorePort.refresh(sessionId, ttl());
    }

    public void validateActiveSession(String sessionId) {
        cleanupStaleSessions();
        if (!isActive(sessionId)) {
            throw new BusinessException(ErrorCode.SESSION_INVALID);
        }
    }

    public int countActiveSessions() {
        cleanupStaleSessions();
        return activeSessionStorePort.findAllSessionIds().size();
    }

    public void cleanupStaleSessions() {
        Set<String> sessionIds = activeSessionStorePort.findAllSessionIds();
        for (String sessionId : sessionIds) {
            if (!activeSessionStorePort.exists(sessionId)) {
                activeSessionStorePort.removeFromIndex(sessionId);
            }
        }
    }

    private Duration ttl() {
        return Duration.ofSeconds(properties.getActiveSessionTtlSeconds());
    }
}
