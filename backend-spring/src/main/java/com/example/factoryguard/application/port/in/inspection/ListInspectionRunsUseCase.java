package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.InspectionRunSummary;
import com.example.factoryguard.application.dto.inspection.ListInspectionRunsQuery;

import java.util.List;

public interface ListInspectionRunsUseCase {

    List<InspectionRunSummary> execute(ListInspectionRunsQuery query);
}
