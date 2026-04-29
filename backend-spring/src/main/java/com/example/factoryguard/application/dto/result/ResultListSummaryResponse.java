package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ResultListSummaryResponse {

    private final long totalCount;
    private final long normalCount;
    private final long defectCount;
    private final long retestCount;
    private final BigDecimal avgScore;
}
