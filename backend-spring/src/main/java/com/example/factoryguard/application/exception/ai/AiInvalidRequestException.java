package com.example.factoryguard.application.exception.ai;

public class AiInvalidRequestException extends RuntimeException {

    private final int status;
    private final String upstreamErrorCode;
    private final String upstreamDetail;
    private final String requestId;

    public AiInvalidRequestException(int status, String message) {
        this(status, message, null, message, null);
    }

    public AiInvalidRequestException(int status, String message, String upstreamErrorCode, String upstreamDetail, String requestId) {
        super(message);
        this.status = status;
        this.upstreamErrorCode = upstreamErrorCode;
        this.upstreamDetail = upstreamDetail;
        this.requestId = requestId;
    }

    public int getStatus() {
        return status;
    }

    public String getUpstreamErrorCode() {
        return upstreamErrorCode;
    }

    public String getUpstreamDetail() {
        return upstreamDetail;
    }

    public String getRequestId() {
        return requestId;
    }
}
