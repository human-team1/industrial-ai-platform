package com.example.factoryguard.adapter.out.redis.inspection;

import com.example.factoryguard.adapter.out.cache.redis.RedisCacheAdapter;
import com.example.factoryguard.application.port.out.inspection.InspectionIdempotencyCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InspectionIdempotencyRedisAdapter implements InspectionIdempotencyCachePort {

    private static final Duration TTL = Duration.ofMinutes(10);

    private final RedisCacheAdapter redisCacheAdapter;

    @Override
    public Optional<String> findFingerprint(Long organizationId, Long userId, String idempotencyKey) {
        try {
            return redisCacheAdapter.get(key(organizationId, userId, idempotencyKey));
        } catch (Exception exception) {
            log.warn("Failed to read inspection idempotency cache, organizationId={}, userId={}",
                    organizationId, userId, exception);
            return Optional.empty();
        }
    }

    @Override
    public boolean reserve(Long organizationId, Long userId, String idempotencyKey, String fingerprint) {
        try {
            return redisCacheAdapter.setIfAbsent(key(organizationId, userId, idempotencyKey), fingerprint, TTL);
        } catch (Exception exception) {
            log.warn("Failed to reserve inspection idempotency cache, organizationId={}, userId={}",
                    organizationId, userId, exception);
            return true;
        }
    }

    private String key(Long organizationId, Long userId, String idempotencyKey) {
        return "inspection:idempotency:" + organizationId + ":" + userId + ":" + idempotencyKey;
    }
}
