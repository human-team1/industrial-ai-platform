package com.example.factoryguard.application.port.out.auth;

import java.time.Duration;
import java.util.Set;

public interface ActiveSessionStorePort {

    void save(String sessionId, String userIdValue, Duration ttl);

    boolean exists(String sessionId);

    void refresh(String sessionId, Duration ttl);

    void delete(String sessionId);

    Set<String> findAllSessionIds();

    void addToIndex(String sessionId);

    void removeFromIndex(String sessionId);
}
