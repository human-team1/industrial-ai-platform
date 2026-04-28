package com.example.factoryguard.application.port.in.user;

import com.example.factoryguard.application.dto.user.UserThresholdHistoryResult;

import java.util.List;

public interface ListUserThresholdHistoryUseCase {

    List<UserThresholdHistoryResult> execute(Long userId, String sessionId);
}
