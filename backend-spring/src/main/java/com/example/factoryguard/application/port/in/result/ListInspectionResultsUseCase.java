package com.example.factoryguard.application.port.in.result;

import com.example.factoryguard.application.dto.result.ListInspectionResultsQuery;
import com.example.factoryguard.application.dto.result.ResultPageResponse;

public interface ListInspectionResultsUseCase {

    ResultPageResponse execute(ListInspectionResultsQuery query);
}
