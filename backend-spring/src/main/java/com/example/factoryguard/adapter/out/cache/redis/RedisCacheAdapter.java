package com.example.factoryguard.adapter.out.cache.redis;

import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheAdapter {

    private final StringRedisTemplate redisTemplate;

    public RedisCacheAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public boolean setIfAbsent(String key, String value, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, ttl));
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean ping() {
        try {
            return Boolean.TRUE.equals(redisTemplate.execute((RedisCallback<Boolean>) connection ->
                    "PONG".equalsIgnoreCase(connection.ping())
            ));
        } catch (Exception exception) {
            return false;
        }
    }
}
