package com.example.factoryguard.application.port.out.signuprequest;

public interface ProcessSignupRequestPort {

    Long approve(Long requestId, Long adminUserId);

    Long reject(Long requestId, Long adminUserId, String rejectReason);
}
