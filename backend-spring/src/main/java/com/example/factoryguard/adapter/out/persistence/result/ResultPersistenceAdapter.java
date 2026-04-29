package com.example.factoryguard.adapter.out.persistence.result;

import com.example.factoryguard.application.dto.result.ListInspectionResultsQuery;
import com.example.factoryguard.application.dto.result.ResultDetailResponse;
import com.example.factoryguard.application.dto.result.ResultPageResponse;
import com.example.factoryguard.application.port.out.result.ResultQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResultPersistenceAdapter implements ResultQueryPort {

    private final ResultQueryRepository resultQueryRepository;

    @Override
    public ResultPageResponse findPage(ListInspectionResultsQuery query, Long organizationId) {
        return resultQueryRepository.findPage(query, organizationId);
    }

    @Override
    public Optional<Long> findOrganizationIdByResultId(Long resultId) {
        return resultQueryRepository.findOrganizationIdByResultId(resultId);
    }

    @Override
    public Optional<ResultDetailResponse> findDetail(Long resultId) {
        return resultQueryRepository.findDetail(resultId);
    }
}
