package com.example.factoryguard.application.port.in.result;

import com.example.factoryguard.application.dto.result.InspectionResultSummary;
import com.example.factoryguard.application.dto.result.ListInspectionResultsQuery;

import java.util.List;

public interface ListInspectionResultsUseCase {

    List<InspectionResultSummary> execute(ListInspectionResultsQuery query);
}
