package com.example.factoryguard.application.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleLoginResult {

    private final AuthStatus userStatus;

    private final String accessToken;

    @JsonIgnore
    private final String refreshToken;

    private final Long userId;
    private final String googleSub;
    private final String email;
    private final String name;
    private final String picture;
    private final String role;

    public static GoogleLoginResult ofNew(GoogleTokenInfo tokenInfo) {
        return GoogleLoginResult.builder()
                .userStatus(AuthStatus.NEW)
                .googleSub(tokenInfo.getSub())
                .email(tokenInfo.getEmail())
                .name(tokenInfo.getName())
                .picture(tokenInfo.getPicture())
                .build();
    }

    public static GoogleLoginResult ofPending(String email, String name) {
        return GoogleLoginResult.builder()
                .userStatus(AuthStatus.PENDING)
                .email(email)
                .name(name)
                .build();
    }

    public static GoogleLoginResult ofRejected(String email, String name) {
        return GoogleLoginResult.builder()
                .userStatus(AuthStatus.REJECTED)
                .email(email)
                .name(name)
                .build();
    }

    public static GoogleLoginResult ofActive(String accessToken, String refreshToken,
                                             Long userId, String googleSub,
                                             String email, String name,
                                             String picture, String role) {
        return GoogleLoginResult.builder()
                .userStatus(AuthStatus.ACTIVE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userId)
                .googleSub(googleSub)
                .email(email)
                .name(name)
                .picture(picture)
                .role(role)
                .build();
    }
}
