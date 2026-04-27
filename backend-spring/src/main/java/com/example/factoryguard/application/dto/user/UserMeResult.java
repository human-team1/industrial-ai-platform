package com.example.factoryguard.application.dto.user;

import com.example.factoryguard.domain.user.model.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserMeResult {

    private final Long userId;
    private final String email;
    private final String name;
    private final String picture;
    private final String phone;
    private final String role;
    private final Long organizationId;
    private final String organizationName;
    private final String status;
    private final LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;

    public static UserMeResult from(User user, String organizationName) {
        return UserMeResult.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .picture(user.getPicture())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .organizationId(user.getOrganizationId())
                .organizationName(organizationName)
                .status(user.getStatus().name())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
