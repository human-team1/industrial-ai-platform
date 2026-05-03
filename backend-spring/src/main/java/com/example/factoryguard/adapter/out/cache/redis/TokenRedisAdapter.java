package com.example.factoryguard.adapter.out.cache.redis;

import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TokenRedisAdapter implements TokenStorePort {

    private final RedisCacheAdapter redisCacheAdapter;
    private final RedisKeyFactory redisKeyFactory;

    @Override
    public void saveRefreshToken(Long userId, String sessionId, String token, Duration ttl) {
        redisCacheAdapter.set(redisKeyFactory.refreshTokenKey(userId, sessionId), token, ttl);
    }

    @Override
    public Optional<String> getRefreshToken(Long userId, String sessionId) {
        return redisCacheAdapter.get(redisKeyFactory.refreshTokenKey(userId, sessionId));
    }

    @Override
    public void deleteRefreshToken(Long userId, String sessionId) {
        redisCacheAdapter.delete(redisKeyFactory.refreshTokenKey(userId, sessionId));
    }

    @Override
    public void saveSessionId(Long userId, String sessionId, Duration ttl) {
        redisCacheAdapter.set(redisKeyFactory.sessionKey(userId, sessionId), sessionId, ttl);
        redisCacheAdapter.set(redisKeyFactory.currentSessionKey(userId), sessionId, ttl);
    }

    @Override
    public Optional<String> getSessionId(Long userId) {
        return redisCacheAdapter.get(redisKeyFactory.currentSessionKey(userId));
    }

    @Override
    public void deleteSessionId(Long userId) {
        Optional<String> currentSessionId = getSessionId(userId);
        currentSessionId.ifPresent(sessionId -> redisCacheAdapter.delete(redisKeyFactory.sessionKey(userId, sessionId)));
        redisCacheAdapter.delete(redisKeyFactory.currentSessionKey(userId));
    }

    @Override
    public void deleteSessionId(Long userId, String sessionId) {
        redisCacheAdapter.delete(redisKeyFactory.sessionKey(userId, sessionId));
        Optional<String> currentSessionId = getSessionId(userId);
        if (currentSessionId.isPresent() && currentSessionId.get().equals(sessionId)) {
            redisCacheAdapter.delete(redisKeyFactory.currentSessionKey(userId));
        }
    }

    @Override
    public boolean hasSessionId(Long userId, String sessionId) {
        return redisCacheAdapter.hasKey(redisKeyFactory.sessionKey(userId, sessionId));
    }

    @Override
    public void refreshSessionId(Long userId, String sessionId, Duration ttl) {
        if (hasSessionId(userId, sessionId)) {
            redisCacheAdapter.expire(redisKeyFactory.sessionKey(userId, sessionId), ttl);
        }
        Optional<String> currentSessionId = getSessionId(userId);
        if (currentSessionId.isPresent() && currentSessionId.get().equals(sessionId)) {
            redisCacheAdapter.expire(redisKeyFactory.currentSessionKey(userId), ttl);
        }
    }
}
