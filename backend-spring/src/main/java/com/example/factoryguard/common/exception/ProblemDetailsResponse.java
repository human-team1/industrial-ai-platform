package com.example.factoryguard.common.exception;

import java.time.OffsetDateTime;

public class ProblemDetailsResponse {

    private final String type;
    private final String title;
    private final int status;
    private final String detail;
    private final String instance;
    private final String code;
    private final OffsetDateTime timestamp;

    public ProblemDetailsResponse(String type, String title, int status, String detail, String instance, String code) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.code = code;
        this.timestamp = OffsetDateTime.now();
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    public String getInstance() {
        return instance;
    }

    public String getCode() {
        return code;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
