package com.example.factoryguard.application.port.out.user;

import com.example.factoryguard.domain.user.model.UserSetting;

import java.util.Optional;

public interface LoadUserSettingPort {

    Optional<UserSetting> findByUserId(Long userId);
}
