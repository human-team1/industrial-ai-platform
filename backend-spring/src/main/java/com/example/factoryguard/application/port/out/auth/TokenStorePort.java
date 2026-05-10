package com.example.factoryguard.application.port.out.auth;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface TokenStorePort {

    void saveRefreshToken(Long userId, String sessionId, String token, Duration ttl);

    Optional<String> getRefreshToken(Long userId, String sessionId);

    void deleteRefreshToken(Long userId, String sessionId);

    void saveSessionId(Long userId, String sessionId, Duration ttl);

    Optional<String> getSessionId(Long userId);

    void deleteSessionId(Long userId);

    void deleteSessionId(Long userId, String sessionId);

    boolean hasSessionId(Long userId, String sessionId);

    void refreshSessionId(Long userId, String sessionId, Duration ttl);

    List<String> findAllSessionIdsByUserId(Long userId);

    void deleteAllRefreshTokensByUserId(Long userId);

    void deleteAllSessionsByUserId(Long userId);
}
