package com.example.factoryguard.application.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupRequestCommand {

    private final String googleSub;
    private final String email;
    private final String name;
    private final String picture;
}
