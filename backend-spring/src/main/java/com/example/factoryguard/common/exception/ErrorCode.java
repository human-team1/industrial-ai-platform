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
    INSPECTION_NOT_FOUND("INSPECTION-404", HttpStatus.NOT_FOUND, "검사를 찾을 수 없습니다."),
    INSPECTION_INVALID_STATE("INSPECTION-409", HttpStatus.CONFLICT, "검사 상태에서 허용되지 않는 동작입니다."),
    IDEMPOTENCY_CONFLICT("INSPECTION-409B", HttpStatus.CONFLICT, "동일 Idempotency-Key로 다른 요청이 이미 처리되었습니다."),
    INVALID_IDEMPOTENCY_KEY("INSPECTION-422", HttpStatus.UNPROCESSABLE_ENTITY, "Idempotency-Key 값이 올바르지 않습니다."),
    INVALID_FILE_EMPTY("FILE-400", HttpStatus.UNPROCESSABLE_ENTITY, "파일이 비어있습니다."),
    INVALID_FILE_SIZE("FILE-401", HttpStatus.UNPROCESSABLE_ENTITY, "허용된 파일 크기를 초과했습니다."),
    INVALID_FILE_MIME("FILE-402", HttpStatus.UNPROCESSABLE_ENTITY, "허용되지 않은 MIME 타입입니다."),
    INVALID_FILE_EXTENSION("FILE-403", HttpStatus.UNPROCESSABLE_ENTITY, "허용되지 않은 파일 확장자입니다."),
    INVALID_FILE_NAME("FILE-404", HttpStatus.UNPROCESSABLE_ENTITY, "파일명이 올바르지 않습니다."),
    CAMERA_NOT_FOUND("CAMERA-404", HttpStatus.NOT_FOUND, "카메라를 찾을 수 없습니다."),
    CAMERA_INVALID_URL("CAMERA-400", HttpStatus.BAD_REQUEST, "스트림 URL 형식이 올바르지 않습니다."),
    AI_SERVER_ERROR("AI-503", HttpStatus.SERVICE_UNAVAILABLE, "AI 서버와 통신 중 오류가 발생했습니다."),
    AI_TIMEOUT("AI-504", HttpStatus.GATEWAY_TIMEOUT, "AI 서버 호출이 시간 초과되었습니다."),
    AI_REQUEST_INVALID("AI-400", HttpStatus.BAD_GATEWAY, "AI 서버 요청 형식이 올바르지 않습니다."),
    REALTIME_NOT_ENABLED("REALTIME-503", HttpStatus.SERVICE_UNAVAILABLE, "실시간 검사는 현재 사용할 수 없습니다."),
    USER_THRESHOLD_NOT_FOUND("USER-THRESHOLD-404", HttpStatus.NOT_FOUND, "사용자 개인 임계값이 없습니다."),
    USER_THRESHOLD_FORBIDDEN("USER-THRESHOLD-403", HttpStatus.FORBIDDEN, "다른 사용자의 임계값에 접근할 수 없습니다."),
    USER_THRESHOLD_INVALID_RANGE("USER-THRESHOLD-422A", HttpStatus.UNPROCESSABLE_ENTITY, "임계값이 허용 범위를 벗어났습니다."),
    USER_THRESHOLD_INVALID_RELATION("USER-THRESHOLD-422B", HttpStatus.UNPROCESSABLE_ENTITY, "lowConfidenceThreshold는 anomalyThreshold보다 작아야 합니다."),
    USER_THRESHOLD_INACTIVE("USER-THRESHOLD-409A", HttpStatus.CONFLICT, "비활성 임계값은 수정할 수 없습니다."),
    USER_THRESHOLD_ALREADY_EXISTS("USER-THRESHOLD-409B", HttpStatus.CONFLICT, "이미 활성화된 개인 임계값이 존재합니다."),
    USER_THRESHOLD_LOW_CONFIDENCE_CHANGE_UNSUPPORTED("USER-THRESHOLD-422C", HttpStatus.UNPROCESSABLE_ENTITY, "lowConfidenceThreshold 변경은 현재 지원하지 않습니다."),
    USER_SETTING_SAVE_FAILED("USER-SETTING-500", HttpStatus.INTERNAL_SERVER_ERROR, "사용자 설정 저장 중 오류가 발생했습니다.");

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
