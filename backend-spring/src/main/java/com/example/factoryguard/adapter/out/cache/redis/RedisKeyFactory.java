package com.example.factoryguard.adapter.out.cache.redis;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyFactory {

    public String sessionKey(long userId, String sessionId) {
        return "session:" + userId + ":" + sessionId;
    }

    public String jobKey(String jobType, String jobId) {
        return "job:" + jobType + ":" + jobId;
    }

    public String idempotencyKey(String domain, String key) {
        return "idempotency:" + domain + ":" + key;
    }

    public String refreshTokenKey(Long userId) {
        return "refresh:token:" + userId;
    }
}
