package com.example.factoryguard.application.port.out.signuprequest;

import java.util.Optional;

public interface FindRejectReasonByUserIdPort {

    Optional<String> findLatestRejectReasonByUserId(Long userId);
}
