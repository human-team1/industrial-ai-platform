package com.example.factoryguard.application.port.out.auth;

import com.example.factoryguard.domain.user.model.AuthSession;

import java.util.Optional;

public interface LoadAuthSessionPort {

    Optional<AuthSession> findByUserId(Long userId);
}
