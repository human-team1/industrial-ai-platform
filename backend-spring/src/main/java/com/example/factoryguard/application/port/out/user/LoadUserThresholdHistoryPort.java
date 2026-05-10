package com.example.factoryguard.application.port.out.user;

import com.example.factoryguard.domain.user.model.UserThresholdHistory;

import java.util.List;
import java.util.Optional;

public interface LoadUserThresholdHistoryPort {

    List<UserThresholdHistory> findAllByThresholdId(Long thresholdId);

    Optional<UserThresholdHistory> findLatestByThresholdId(Long thresholdId);
}

