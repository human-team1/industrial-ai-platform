package com.example.factoryguard.application.service.result;

import com.example.factoryguard.application.dto.result.ListInspectionResultsQuery;
import com.example.factoryguard.application.dto.result.ResultListSummaryResponse;
import com.example.factoryguard.application.dto.result.ResultPageResponse;
import com.example.factoryguard.application.port.out.result.LoadAnomalyRegionPort;
import com.example.factoryguard.application.port.out.result.LoadResultArtifactPort;
import com.example.factoryguard.application.port.out.result.LoadResultImagePort;
import com.example.factoryguard.application.port.out.result.ResultQueryPort;
import com.example.factoryguard.config.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultQueryServiceTest {

    @Mock ResultQueryPort resultQueryPort;
    @Mock LoadResultArtifactPort loadResultArtifactPort;
    @Mock LoadResultImagePort loadResultImagePort;
    @Mock LoadAnomalyRegionPort loadAnomalyRegionPort;
    @Mock SecurityUtils securityUtils;

    ResultQueryService service;

    @BeforeEach
    void setUp() {
        service = new ResultQueryService(
                resultQueryPort,
                loadResultArtifactPort,
                loadResultImagePort,
                loadAnomalyRegionPort,
                securityUtils
        );
    }

    @Test
    void resultListCanBeEmpty() {
        ListInspectionResultsQuery query = new ListInspectionResultsQuery(
                null, null, null, null, null, null, null, null, null, 0, 20
        );
        ResultPageResponse emptyPage = ResultPageResponse.builder()
                .content(List.of())
                .page(0)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .summary(ResultListSummaryResponse.builder().totalCount(0).build())
                .build();
        when(securityUtils.isSiteAdmin()).thenReturn(false);
        when(securityUtils.requireOrganizationId()).thenReturn(1L);
        when(resultQueryPort.findPage(query, 1L)).thenReturn(emptyPage);

        ResultPageResponse result = service.execute(query);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
