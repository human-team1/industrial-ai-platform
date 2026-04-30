package com.example.factoryguard.application.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class EquipmentScoreTrendResult {

    private final Long targetId;
    private final String equipmentName;
    private final LocalDate date;
    private final Double averageScore;
}
