package com.example.factoryguard.application.dto.signup;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SignupRequestSummary {

    private final Long requestId;
    private final Long userId;
    private final String name;
    private final String email;
    private final String picture;
    private final LocalDateTime requestedAt;
}
