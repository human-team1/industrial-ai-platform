package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.port.out.inspection.SaveInspectionInputPort;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InspectionInputRecorder {

    private final SaveInspectionInputPort saveInspectionInputPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public InspectionInput record(InspectionInput input) {
        return saveInspectionInputPort.save(input);
    }
}
