package com.example.factoryguard.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_ERROR("COMMON-500", HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error"),
    INVALID_REQUEST("COMMON-400", HttpStatus.BAD_REQUEST, "Invalid request"),
    RESOURCE_NOT_FOUND("COMMON-404", HttpStatus.NOT_FOUND, "Resource not found"),
    UNAUTHORIZED("AUTH-401", HttpStatus.UNAUTHORIZED, "Authentication required"),
    INVALID_GOOGLE_TOKEN("AUTH-402", HttpStatus.UNAUTHORIZED, "Invalid Google ID token"),
    INVALID_REFRESH_TOKEN("AUTH-403", HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"),
    INVALID_SIGNUP_TOKEN("AUTH-404", HttpStatus.UNAUTHORIZED, "Invalid or expired signup token");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}