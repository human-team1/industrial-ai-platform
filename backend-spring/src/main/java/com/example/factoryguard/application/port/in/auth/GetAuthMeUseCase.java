package com.example.factoryguard.application.port.in.auth;

import com.example.factoryguard.application.dto.auth.AuthMeResult;

public interface GetAuthMeUseCase {
    AuthMeResult execute(Long userId, String sessionId);
}