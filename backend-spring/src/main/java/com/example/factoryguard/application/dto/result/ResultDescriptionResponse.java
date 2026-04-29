package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultDescriptionResponse {

    private final String summary;
    private final String recommendedAction;
}
