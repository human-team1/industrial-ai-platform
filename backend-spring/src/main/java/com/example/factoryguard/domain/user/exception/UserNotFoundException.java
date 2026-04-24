package com.example.factoryguard.domain.user.exception;

import com.example.factoryguard.common.BusinessException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Long userId) {
        super("사용자를 찾을 수 없습니다. ID: " + userId, HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String email) {
        super("사용자를 찾을 수 없습니다. Email: " + email, HttpStatus.NOT_FOUND);
    }
}