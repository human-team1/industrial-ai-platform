package com.example.factoryguard.application.port.in.auth;

import com.example.factoryguard.application.dto.auth.SignupRequestCommand;
import com.example.factoryguard.application.dto.auth.SignupRequestResult;

public interface SignupRequestUseCase {

    SignupRequestResult execute(SignupRequestCommand command);
}
