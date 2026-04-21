package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.port.in.inspection.GetInspectionStatusUseCase;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionStatusPort;
import com.example.factoryguard.domain.inspection.model.InspectionStatus;
import org.springframework.stereotype.Service;

@Service
public class InspectionStatusService implements GetInspectionStatusUseCase {

    private final LoadInspectionStatusPort loadInspectionStatusPort;

    public InspectionStatusService(LoadInspectionStatusPort loadInspectionStatusPort) {
        this.loadInspectionStatusPort = loadInspectionStatusPort;
    }

    @Override
    public InspectionStatus getStatus() {
        return loadInspectionStatusPort.loadStatus();
    }
}
