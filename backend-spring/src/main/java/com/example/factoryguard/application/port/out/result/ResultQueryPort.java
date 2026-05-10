package com.example.factoryguard.application.port.out.result;

import com.example.factoryguard.application.dto.result.ListInspectionResultsQuery;
import com.example.factoryguard.application.dto.result.ResultDetailResponse;
import com.example.factoryguard.application.dto.result.ResultPageResponse;

import java.util.Optional;

public interface ResultQueryPort {

    ResultPageResponse findPage(ListInspectionResultsQuery query, Long organizationId);

    Optional<Long> findOrganizationIdByResultId(Long resultId);

    Optional<ResultDetailResponse> findDetail(Long resultId);
}
