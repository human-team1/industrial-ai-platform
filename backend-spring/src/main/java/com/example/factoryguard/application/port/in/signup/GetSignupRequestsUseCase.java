package com.example.factoryguard.application.port.in.signup;

import com.example.factoryguard.application.dto.signup.SignupRequestSummary;

import java.util.List;

public interface GetSignupRequestsUseCase {

    List<SignupRequestSummary> execute();
}
