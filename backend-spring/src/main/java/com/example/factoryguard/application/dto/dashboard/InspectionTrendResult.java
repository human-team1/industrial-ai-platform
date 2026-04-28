package com.example.factoryguard.application.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class InspectionTrendResult {

    private final LocalDate date;
    private final Long inspectionCount;
    private final Long defectCount;
}
