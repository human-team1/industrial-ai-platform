package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.ListModelVersionsQuery;
import com.example.factoryguard.application.dto.model.ModelPageResponse;
import com.example.factoryguard.application.dto.model.ModelVersionSummaryResponse;

public interface ListModelVersionsUseCase {

    ModelPageResponse<ModelVersionSummaryResponse> listModelVersions(ListModelVersionsQuery query);
}
