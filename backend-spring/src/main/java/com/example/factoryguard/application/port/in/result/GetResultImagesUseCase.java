package com.example.factoryguard.application.port.in.result;

import com.example.factoryguard.application.dto.result.ResultImageResult;

import java.util.List;

public interface GetResultImagesUseCase {

    List<ResultImageResult> execute(Long resultId);
}
