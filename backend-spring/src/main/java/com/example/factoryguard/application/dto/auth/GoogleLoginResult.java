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
    private final Long organizationId;

    /** 신규 유저용: googleSub은 signupToken에만 포함, 응답에는 노출하지 않음 */
    private final String signupToken;

    public static GoogleLoginResult ofNew(GoogleTokenInfo tokenInfo, String signupToken) {
        return GoogleLoginResult.builder()
                .userStatus(AuthStatus.NEW)
                .email(tokenInfo.getEmail())
                .name(tokenInfo.getName())
                .picture(tokenInfo.getPicture())
                .signupToken(signupToken)
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
                                             String picture, String role,
                                             Long organizationId) {
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
                .organizationId(organizationId)
                .build();
    }
}