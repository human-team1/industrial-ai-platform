package com.example.factoryguard.application.port.in.result;

import com.example.factoryguard.application.dto.result.InspectionResultDetail;

public interface GetInspectionResultDetailUseCase {

    InspectionResultDetail execute(Long resultId);
}
