package com.example.factoryguard.domain.user.exception;

import com.example.factoryguard.common.exception.BusinessException; 
import com.example.factoryguard.common.exception.ErrorCode;

public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String email) {
        super(ErrorCode.USER_ALREADY_EXISTS, "이미 존재하는 사용자입니다: " + email); 
    }

    //  byGoogleSub() 추가
    public static UserAlreadyExistsException byGoogleSub(String googleSub) {
        return new UserAlreadyExistsException(
            ErrorCode.USER_ALREADY_EXISTS_GOOGLE_SUB,
            "이미 가입된 Google 계정입니다: " + googleSub
        );
    }

    private UserAlreadyExistsException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}