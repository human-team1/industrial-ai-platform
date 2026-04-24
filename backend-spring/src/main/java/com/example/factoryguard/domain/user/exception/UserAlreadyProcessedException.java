package com.example.factoryguard.domain.user.exception;

import com.example.factoryguard.common.BusinessException;
import org.springframework.http.HttpStatus;

public class UserAlreadyProcessedException extends BusinessException {
    public UserAlreadyProcessedException(Long userId) {
        super("이미 처리된 사용자입니다. ID: " + userId, HttpStatus.BAD_REQUEST);
    }
}