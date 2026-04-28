package com.example.factoryguard.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_ERROR("COMMON-500", HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error"),
    INVALID_REQUEST("COMMON-400", HttpStatus.BAD_REQUEST, "Invalid request"),
    RESOURCE_NOT_FOUND("COMMON-404", HttpStatus.NOT_FOUND, "Resource not found"),

   
    USER_ALREADY_EXISTS("USER-409", HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
    USER_ALREADY_EXISTS_GOOGLE_SUB("USER-409-G", HttpStatus.CONFLICT, "이미 가입된 Google 계정입니다.");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
    public String getDefaultMessage() { return defaultMessage; }
}