package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.AvailableInspectionModelItem;

import java.util.List;

public interface GetAvailableInspectionModelsUseCase {
    List<AvailableInspectionModelItem> execute(Long organizationId, Long targetId, String inspectionType, String modelCategory);
}
