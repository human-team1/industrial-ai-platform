package com.example.factoryguard.application.port.out.signuprequest;

import com.example.factoryguard.domain.user.model.SignupRequest;

import java.util.Optional;

public interface LoadSignupRequestPort {

    Optional<SignupRequest> findById(Long signupRequestId);
}
