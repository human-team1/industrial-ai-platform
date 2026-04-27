package com.example.factoryguard.application.dto.auth;

import com.example.factoryguard.domain.user.model.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthMeResult {

    private final Long userId;
    private final String name;
    private final String role;
    private final String status;
    private final Long organizationId;

    public static AuthMeResult from(User user) {
        return AuthMeResult.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .organizationId(user.getOrganizationId())
                .build();
    }
}