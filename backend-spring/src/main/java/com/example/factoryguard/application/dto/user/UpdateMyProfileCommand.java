package com.example.factoryguard.application.dto.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateMyProfileCommand {

    private final Long userId;
    private final String sessionId;
    private final String name;
    private final String phone;
    private final String picture;
}
