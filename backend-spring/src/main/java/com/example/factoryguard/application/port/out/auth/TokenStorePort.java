package com.example.factoryguard.application.port.out.auth;

import java.time.Duration;
import java.util.Optional;

public interface TokenStorePort {

    void saveRefreshToken(Long userId, String token, Duration ttl);

    Optional<String> getRefreshToken(Long userId);

    void deleteRefreshToken(Long userId);
}
