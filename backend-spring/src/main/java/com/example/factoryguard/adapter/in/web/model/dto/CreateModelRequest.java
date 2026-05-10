package com.example.factoryguard.adapter.in.web.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateModelRequest {

    private String modelName;
    private String modelType;
    private String description;
}
