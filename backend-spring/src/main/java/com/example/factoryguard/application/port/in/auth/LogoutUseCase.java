package com.example.factoryguard.application.port.in.auth;

public interface LogoutUseCase {

    void execute(Long userId, String sessionId);
}
