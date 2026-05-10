package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateModelCommand {

    private final String modelName;
    private final String modelType;
    private final String description;
}
