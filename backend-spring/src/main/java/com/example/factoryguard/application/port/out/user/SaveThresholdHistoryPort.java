package com.example.factoryguard.application.port.out.user;

import com.example.factoryguard.domain.user.model.UserThresholdHistory;

public interface SaveThresholdHistoryPort {

    void save(UserThresholdHistory history);
}