package com.example.factoryguard.adapter.out.redis.auth;

import com.example.factoryguard.adapter.out.cache.redis.RedisKeyFactory;
import com.example.factoryguard.application.port.out.auth.ActiveSessionStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisActiveSessionStoreAdapter implements ActiveSessionStorePort {

    private final StringRedisTemplate redisTemplate;
    private final RedisKeyFactory redisKeyFactory;

    @Override
    public void save(String sessionId, String userIdValue, Duration ttl) {
        redisTemplate.opsForValue().set(redisKeyFactory.activeSessionKey(sessionId), userIdValue, ttl);
    }

    @Override
    public boolean exists(String sessionId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKeyFactory.activeSessionKey(sessionId)));
    }

    @Override
    public void refresh(String sessionId, Duration ttl) {
        redisTemplate.expire(redisKeyFactory.activeSessionKey(sessionId), ttl);
    }

    @Override
    public void delete(String sessionId) {
        redisTemplate.delete(redisKeyFactory.activeSessionKey(sessionId));
    }

    @Override
    public Set<String> findAllSessionIds() {
        Set<String> members = redisTemplate.opsForSet().members(redisKeyFactory.activeSessionsKey());
        return members == null ? Set.of() : members;
    }

    @Override
    public void addToIndex(String sessionId) {
        redisTemplate.opsForSet().add(redisKeyFactory.activeSessionsKey(), sessionId);
    }

    @Override
    public void removeFromIndex(String sessionId) {
        redisTemplate.opsForSet().remove(redisKeyFactory.activeSessionsKey(), sessionId);
    }
}
