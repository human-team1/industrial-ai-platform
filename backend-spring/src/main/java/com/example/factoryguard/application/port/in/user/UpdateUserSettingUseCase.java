package com.example.factoryguard.application.port.in.user;

import com.example.factoryguard.application.dto.user.UpdateUserSettingCommand;
import com.example.factoryguard.application.dto.user.UserSettingResult;

public interface UpdateUserSettingUseCase {

    UserSettingResult execute(UpdateUserSettingCommand command);
}
