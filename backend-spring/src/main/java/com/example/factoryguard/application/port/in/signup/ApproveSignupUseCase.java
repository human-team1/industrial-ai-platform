package com.example.factoryguard.application.port.in.signup;

public interface ApproveSignupUseCase {

    void execute(Long requestId, Long adminUserId, Long organizationId);
}