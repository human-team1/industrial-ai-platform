package com.example.factoryguard.adapter.in.web.dto;

import com.example.factoryguard.application.port.in.dto.SignupResult;

public class SignupResponse {
    private final Long id;
    private final String email;
    private final String name;
    private final String picture;
    private final String status;

    public SignupResponse(Long id, String email, String name, String picture, String status) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.status = status;
    }

    /**
     * Application 계층의 Result 객체를 Web 계층의 Response 객체로 변환합니다.
     */
    public static SignupResponse from(SignupResult result) {
        return new SignupResponse(
            result.getId(),
            result.getEmail(),
            result.getName(),
            result.getPicture(),
            result.getStatus()
        );
    }

    // Getter들 (No Lombok 컨벤션 준수)
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

    public String getStatus() {
        return status;
    }
}