package com.example.factoryguard.application.port.out.user;

import com.example.factoryguard.domain.user.model.UserStatus;

public interface UpdateUserStatusPort {

    void updateStatus(Long userId, UserStatus status);
}
