package com.example.factoryguard.application.port.in.user;

import com.example.factoryguard.application.dto.user.UserSettingResult;

public interface GetUserSettingUseCase {

    UserSettingResult execute(Long userId, String sessionId);
}
