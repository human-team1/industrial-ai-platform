package com.example.factoryguard.application.port.in.auth;

import com.example.factoryguard.application.dto.auth.GoogleLoginCommand;
import com.example.factoryguard.application.dto.auth.GoogleLoginResult;

public interface GoogleLoginUseCase {

    GoogleLoginResult execute(GoogleLoginCommand command);
}
