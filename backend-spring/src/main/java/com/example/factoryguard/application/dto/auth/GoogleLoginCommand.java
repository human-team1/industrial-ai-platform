package com.example.factoryguard.application.dto.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GoogleLoginCommand {

    private final String idToken;
}
