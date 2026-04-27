package com.example.factoryguard.application.port.out.user;

import com.example.factoryguard.domain.user.model.UserThreshold;

import java.util.Optional;

public interface LoadThresholdByIdPort {

    Optional<UserThreshold> findById(Long thresholdId);
}