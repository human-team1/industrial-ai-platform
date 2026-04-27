package com.example.factoryguard.application.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthMeResult {

    private final Long userId;
    private final String role;
    private final Long organizationId;
}