package com.example.factoryguard.adapter.in.web.dto;

import com.example.factoryguard.application.port.in.dto.SignupResult;
import com.example.factoryguard.domain.user.UserStatus;

public class SignupResponse {
    private Long userId;
    private String email;
    private String name;
    private String status;

    public SignupResponse() {}

    public SignupResponse(Long userId, String email, String name, String status) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.status = status;
    }

    public static SignupResponse from(SignupResult result) {
        return new SignupResponse(
            result.getUserId(),
            result.getEmail(),
            result.getName(),
            result.getStatus().getValue()
        );
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}