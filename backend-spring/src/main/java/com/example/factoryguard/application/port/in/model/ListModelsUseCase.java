package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.ListModelsQuery;
import com.example.factoryguard.application.dto.model.ModelPageResponse;
import com.example.factoryguard.application.dto.model.ModelSummaryResponse;

public interface ListModelsUseCase {

    ModelPageResponse<ModelSummaryResponse> listModels(ListModelsQuery query);
}
