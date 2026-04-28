package com.example.factoryguard.application.port.in.dashboard;

import com.example.factoryguard.application.dto.dashboard.EquipmentScoreTrendResult;

import java.util.List;

public interface GetEquipmentScoreTrendUseCase {

    List<EquipmentScoreTrendResult> execute(Long organizationId, Long targetId);
}
