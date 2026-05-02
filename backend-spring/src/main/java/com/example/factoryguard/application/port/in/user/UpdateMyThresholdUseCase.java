package com.example.factoryguard.application.port.in.user;

import com.example.factoryguard.application.dto.user.UpdateThresholdCommand;
import com.example.factoryguard.application.dto.user.UserThresholdResult;

public interface UpdateMyThresholdUseCase {

    UserThresholdResult execute(Long userId, String sessionId, Long thresholdId, UpdateThresholdCommand command);
}
