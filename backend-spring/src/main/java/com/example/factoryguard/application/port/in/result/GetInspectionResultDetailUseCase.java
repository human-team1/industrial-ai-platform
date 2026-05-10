package com.example.factoryguard.application.port.in.result;

import com.example.factoryguard.application.dto.result.ResultDetailResponse;

public interface GetInspectionResultDetailUseCase {

    ResultDetailResponse execute(Long resultId);
}
