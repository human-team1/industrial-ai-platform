package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListModelVersionsQuery {

    private final Long modelId;
    private final String modelCategory;
    private final String modelProfile;
    private final String deployStatus;
    private final Boolean isActive;
    private final int page;
    private final int size;
    private final String sort;
}
