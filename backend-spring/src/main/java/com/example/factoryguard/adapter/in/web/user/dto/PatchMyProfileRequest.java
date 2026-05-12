package com.example.factoryguard.adapter.in.web.user.dto;

import com.example.factoryguard.application.dto.user.UpdateMyProfileCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PatchMyProfileRequest {

    private String name;
    private String phone;

    public UpdateMyProfileCommand toCommand(Long userId, String sessionId) {
        return new UpdateMyProfileCommand(userId, sessionId, name, phone, null);
    }
}
