package com.example.factoryguard.application.port.out.user;

import com.example.factoryguard.domain.user.model.UserThreshold;

import java.util.Optional;

public interface LoadUserThresholdPort {

    Optional<UserThreshold> findActiveByUserId(Long userId);
}
