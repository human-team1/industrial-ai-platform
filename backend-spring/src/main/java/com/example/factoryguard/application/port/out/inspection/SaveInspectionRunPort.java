package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionRun;

public interface SaveInspectionRunPort {

    InspectionRun save(InspectionRun inspectionRun);
}