package com.example.factoryguard.application.port.in.user;

import com.example.factoryguard.application.dto.user.UserThresholdResult;

public interface GetUserThresholdUseCase {

    UserThresholdResult execute(Long userId, String sessionId);
}
