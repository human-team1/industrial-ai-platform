package com.example.factoryguard.application.port.in.result;

import com.example.factoryguard.application.dto.result.ResultDescriptionResponse;

public interface GetResultExplanationUseCase {

    ResultDescriptionResponse getExplanation(Long resultId);
}
