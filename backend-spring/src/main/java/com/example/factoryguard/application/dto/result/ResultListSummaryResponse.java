package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultListSummaryResponse {

    private final long totalCount;
    private final long normalCount;
    private final long defectCount;
    private final long retestCount;
    private final Double avgScore;
}
