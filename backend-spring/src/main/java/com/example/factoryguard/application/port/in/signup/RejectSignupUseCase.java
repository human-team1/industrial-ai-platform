package com.example.factoryguard.application.port.in.signup;

public interface RejectSignupUseCase {

    void execute(Long requestId, Long adminUserId, String rejectReason);
}
