package com.example.factoryguard.application.port.in.user;

import com.example.factoryguard.application.dto.user.UpdateMyProfileCommand;
import com.example.factoryguard.application.dto.user.UserMeResult;

public interface UpdateMyProfileUseCase {

    UserMeResult execute(UpdateMyProfileCommand command);
}
