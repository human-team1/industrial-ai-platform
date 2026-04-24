package com.example.factoryguard.application.port.in;

import com.example.factoryguard.application.port.in.dto.SignupCommand;
import com.example.factoryguard.application.port.in.dto.SignupResult;

public interface SignupUseCase {
    SignupResult signup(SignupCommand command);
}