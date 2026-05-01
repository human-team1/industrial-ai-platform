package com.example.factoryguard.application.port.in.user;

import com.example.factoryguard.application.dto.user.CreateThresholdCommand;
import com.example.factoryguard.application.dto.user.UserThresholdResult;

public interface CreateMyThresholdUseCase {

    UserThresholdResult execute(Long userId, String sessionId, CreateThresholdCommand command);
}
