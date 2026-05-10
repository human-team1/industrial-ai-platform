package com.example.factoryguard.adapter.in.web.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class GoogleLoginRequest {

    @NotBlank
    private String idToken;
}
