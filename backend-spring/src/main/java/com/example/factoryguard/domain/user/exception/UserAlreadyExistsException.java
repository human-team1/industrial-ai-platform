package com.example.factoryguard.domain.user.exception;

import com.example.factoryguard.common.BusinessException;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String email) {
        super("이미 존재하는 사용자입니다: " + email, HttpStatus.CONFLICT);
    }
}