package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListModelsQuery {

    private final String modelType;
    private final int page;
    private final int size;
    private final String sort;
}
