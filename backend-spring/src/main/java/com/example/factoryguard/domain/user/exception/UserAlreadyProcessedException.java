package com.example.factoryguard.domain.user.exception;

import com.example.factoryguard.common.exception.BusinessException; 
import com.example.factoryguard.common.exception.ErrorCode;

public class UserAlreadyProcessedException extends BusinessException {

    public UserAlreadyProcessedException(Long userId) {
        super(ErrorCode.INVALID_REQUEST, 
            String.format("이미 승인 또는 거절 처리가 완료된 사용자입니다. (ID: %d)", userId));
    }

    public UserAlreadyProcessedException(Long userId, String status) {
        super(ErrorCode.INVALID_REQUEST, 
            String.format("이미 %s 상태인 사용자입니다. (ID: %d)", status, userId));
    }
}