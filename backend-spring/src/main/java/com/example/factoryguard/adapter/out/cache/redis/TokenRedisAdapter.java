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
    public void saveRefreshToken(Long userId, String token, Duration ttl) {
        redisCacheAdapter.set(redisKeyFactory.refreshTokenKey(userId), token, ttl);
    }

    @Override
    public Optional<String> getRefreshToken(Long userId) {
        return redisCacheAdapter.get(redisKeyFactory.refreshTokenKey(userId));
    }

    @Override
    public void deleteRefreshToken(Long userId) {
        redisCacheAdapter.delete(redisKeyFactory.refreshTokenKey(userId));
    }
}
