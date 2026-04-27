package com.example.factoryguard.application.port.out.user;

import com.example.factoryguard.domain.user.model.UserThreshold;

public interface UpdateThresholdPort {

    UserThreshold update(UserThreshold threshold);
}