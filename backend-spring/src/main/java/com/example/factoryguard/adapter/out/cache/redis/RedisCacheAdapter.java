package com.example.factoryguard.adapter.out.cache.redis;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheAdapter {

    private static final long SCAN_COUNT = 256L;

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

    public void deleteAll(Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        redisTemplate.delete(keys);
    }

    public Set<String> scanKeys(String matchPattern) {
        Set<String> keys = new LinkedHashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(matchPattern).count(SCAN_COUNT).build();
        try (Cursor<byte[]> cursor = redisTemplate.executeWithStickyConnection(connection ->
                connection.scan(options))) {
            if (cursor == null) {
                return keys;
            }
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
        }
        return keys;
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void expire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
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
