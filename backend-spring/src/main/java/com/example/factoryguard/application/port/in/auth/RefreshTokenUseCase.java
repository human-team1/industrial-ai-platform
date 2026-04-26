package com.example.factoryguard.application.port.in.auth;

import com.example.factoryguard.application.dto.auth.RefreshTokenResult;

public interface RefreshTokenUseCase {

    RefreshTokenResult execute(String refreshToken);
}
