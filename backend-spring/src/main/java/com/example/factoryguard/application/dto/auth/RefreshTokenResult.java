package com.example.factoryguard.application.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshTokenResult {

    private final String accessToken;
}
