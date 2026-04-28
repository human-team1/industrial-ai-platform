package com.example.factoryguard.application.port.out.user;

import com.example.factoryguard.domain.user.model.UserSetting;

public interface SaveUserSettingPort {

    UserSetting save(UserSetting userSetting);
}
