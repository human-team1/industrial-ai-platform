package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.application.port.out.inspection.LoadInspectionStatusPort;
import com.example.factoryguard.domain.inspection.model.InspectionStatus;
import org.springframework.stereotype.Component;

@Component
public class InspectionStatusPersistenceAdapter implements LoadInspectionStatusPort {

    @Override
    public InspectionStatus loadStatus() {
        return new InspectionStatus("inspection", "READY");
    }
}
