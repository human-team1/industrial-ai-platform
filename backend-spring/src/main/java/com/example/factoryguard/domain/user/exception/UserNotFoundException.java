package com.example.factoryguard.domain.user.exception;

import com.example.factoryguard.common.exception.BusinessException; 
import com.example.factoryguard.common.exception.ErrorCode;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Long userId) {
        super(ErrorCode.RESOURCE_NOT_FOUND, 
            String.format("사용자를 찾을 수 없습니다. (ID: %d)", userId));
    }

    public UserNotFoundException(String email) {
        super(ErrorCode.RESOURCE_NOT_FOUND, 
            String.format("사용자를 찾을 수 없습니다. (Email: %s)", email));
    }

    public static UserNotFoundException byGoogleSub(String googleSub) {
        return new UserNotFoundException(
            ErrorCode.RESOURCE_NOT_FOUND,
            "Google 계정 정보를 찾을 수 없습니다. (Sub: " + googleSub + ")"
        );
    }

    private UserNotFoundException(ErrorCode errorCode, String message) { 
        super(errorCode, message);
    }
}