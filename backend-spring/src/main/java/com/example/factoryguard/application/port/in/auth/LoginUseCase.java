package com.example.factoryguard.application.port.in.auth;

import com.example.factoryguard.application.dto.auth.LoginCommand;
import com.example.factoryguard.application.dto.auth.LoginResult;

public interface LoginUseCase {

    LoginResult execute(LoginCommand command);
}
