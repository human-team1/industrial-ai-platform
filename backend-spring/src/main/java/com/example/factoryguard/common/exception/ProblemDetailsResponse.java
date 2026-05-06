package com.example.factoryguard.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.MDC;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetailsResponse {

    private final String type;
    private final String title;
    private final int status;
    private final String detail;
    private final String instance;
    @JsonProperty("errorCode")
    private final String code;
    private final String requestId;
    private final OffsetDateTime timestamp;
    private final List<ValidationFieldError> errors;
    private final Map<String, Object> extensions;

    public ProblemDetailsResponse(String type, String title, int status, String detail, String instance, String code) {
        this(type, title, status, detail, instance, code, null);
    }

    public ProblemDetailsResponse(String type, String title, int status, String detail, String instance, String code,
                                  List<ValidationFieldError> errors) {
        this(type, title, status, detail, instance, code, errors, null);
    }

    public ProblemDetailsResponse(String type, String title, int status, String detail, String instance, String code,
                                  List<ValidationFieldError> errors, Map<String, Object> extensions) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.code = code;
        this.requestId = MDC.get("requestId");
        this.timestamp = OffsetDateTime.now();
        this.errors = errors;
        this.extensions = extensions;
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

    public String getRequestId() {
        return requestId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public List<ValidationFieldError> getErrors() {
        return errors;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtensions() {
        return extensions == null ? Collections.emptyMap() : extensions;
    }
}
