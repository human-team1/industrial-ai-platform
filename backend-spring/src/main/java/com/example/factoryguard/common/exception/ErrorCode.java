package com.example.factoryguard.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_ERROR("COMMON-500", HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error"),
    INVALID_REQUEST("COMMON-400", HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_FAILED("VALIDATION-422", HttpStatus.UNPROCESSABLE_ENTITY, "유효성 검증에 실패했습니다."),
    RESOURCE_NOT_FOUND("COMMON-404", HttpStatus.NOT_FOUND, "Resource not found"),
    UNAUTHORIZED("AUTH-401", HttpStatus.UNAUTHORIZED, "Authentication required"),
    INVALID_GOOGLE_TOKEN("AUTH-402", HttpStatus.UNAUTHORIZED, "Invalid Google ID token"),
    INVALID_REFRESH_TOKEN("AUTH-403", HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"),
    INVALID_SIGNUP_TOKEN("AUTH-404", HttpStatus.UNAUTHORIZED, "Invalid or expired signup token"),
    PENDING_APPROVAL("AUTH-405", HttpStatus.FORBIDDEN, "가입 승인 대기 중입니다."),
    ACCOUNT_REJECTED("AUTH-406", HttpStatus.FORBIDDEN, "가입이 거절된 계정입니다."),
    SESSION_INVALID("AUTH-407", HttpStatus.UNAUTHORIZED, "세션이 유효하지 않습니다."),
    ACCOUNT_INACTIVE("AUTH-408", HttpStatus.FORBIDDEN, "비활성화된 계정입니다."),
    FORBIDDEN("COMMON-403", HttpStatus.FORBIDDEN, "Access denied"),
    ORGANIZATION_NOT_FOUND("ORG-404", HttpStatus.NOT_FOUND, "조직을 찾을 수 없습니다."),
    ORGANIZATION_INACTIVE("ORG-422", HttpStatus.UNPROCESSABLE_ENTITY, "비활성화된 조직입니다."),
    THRESHOLD_NOT_FOUND("THRESHOLD-404", HttpStatus.NOT_FOUND, "임계값을 찾을 수 없습니다."),
    THRESHOLD_OUT_OF_RANGE("THRESHOLD-400", HttpStatus.BAD_REQUEST, "임계값이 허용 범위를 벗어났습니다."),
    TARGET_NOT_FOUND("TARGET-404", HttpStatus.NOT_FOUND, "분석 대상을 찾을 수 없습니다."),
    INSPECTION_FAILED("INSPECTION-500", HttpStatus.INTERNAL_SERVER_ERROR, "검사 처리 중 오류가 발생했습니다."),
    AI_SERVER_ERROR("AI-503", HttpStatus.SERVICE_UNAVAILABLE, "AI 서버와 통신 중 오류가 발생했습니다.");

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
