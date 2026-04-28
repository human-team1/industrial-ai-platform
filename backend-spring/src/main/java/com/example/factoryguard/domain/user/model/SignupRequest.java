package com.example.factoryguard.domain.user.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SignupRequest {

    private final Long signupRequestId;
    private final Long userId;
    private final Long organizationId;
    private final SignupRequestStatus requestStatus;
    private final String rejectReason;
    private final Long processedBy;
    private final LocalDateTime requestedAt;
    private final LocalDateTime processedAt;
}
