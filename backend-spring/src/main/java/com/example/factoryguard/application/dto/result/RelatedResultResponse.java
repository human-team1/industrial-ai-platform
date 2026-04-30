package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RelatedResultResponse {

    private final Long resultId;
    private final LocalDateTime createdAt;
    private final Double score;
    private final String decisionCode;
    private final String location;
}
