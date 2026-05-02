package com.example.factoryguard.application.port.out.inspection;

import java.util.Optional;

public interface InspectionIdempotencyCachePort {

    Optional<String> findFingerprint(Long organizationId, Long userId, String idempotencyKey);

    boolean reserve(Long organizationId, Long userId, String idempotencyKey, String fingerprint);
}
