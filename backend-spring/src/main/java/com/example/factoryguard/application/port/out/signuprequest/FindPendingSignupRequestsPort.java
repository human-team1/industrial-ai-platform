package com.example.factoryguard.application.port.out.signuprequest;

import com.example.factoryguard.application.dto.signup.SignupRequestSummary;

import java.util.List;

public interface FindPendingSignupRequestsPort {

    List<SignupRequestSummary> findPending();
}
