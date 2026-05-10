package com.example.factoryguard.application.dto.review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewModelInfo {

    private final Long modelVersionId;
    private final String modelName;
    private final String versionName;
    private final String modelCategory;
    private final String modelProfile;
}
