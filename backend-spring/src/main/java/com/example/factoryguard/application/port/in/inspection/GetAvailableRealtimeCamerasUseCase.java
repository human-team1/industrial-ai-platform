package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.AvailableRealtimeCameraItem;

import java.util.List;

public interface GetAvailableRealtimeCamerasUseCase {
    List<AvailableRealtimeCameraItem> execute(Long organizationId, Long targetId);
}
