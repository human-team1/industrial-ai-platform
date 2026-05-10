package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultChecklistItemResponse {

    private final String title;
    private final String description;
    private final String priority;
}
