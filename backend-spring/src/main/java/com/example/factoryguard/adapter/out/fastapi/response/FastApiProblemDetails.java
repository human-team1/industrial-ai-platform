package com.example.factoryguard.adapter.out.fastapi.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class FastApiProblemDetails {

    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;
    private String errorCode;
    private String requestId;
    private Map<String, Object> errors;
}
