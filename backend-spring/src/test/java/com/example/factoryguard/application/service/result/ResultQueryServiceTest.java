package com.example.factoryguard.application.service.result;

import com.example.factoryguard.application.dto.result.ListInspectionResultsQuery;
import com.example.factoryguard.application.dto.result.ResultDecisionResponse;
import com.example.factoryguard.application.dto.result.ResultDetailResponse;
import com.example.factoryguard.application.dto.result.ResultListSummaryResponse;
import com.example.factoryguard.application.dto.result.ResultPageResponse;
import com.example.factoryguard.application.port.out.result.LoadAnomalyRegionPort;
import com.example.factoryguard.application.port.out.result.LoadResultArtifactPort;
import com.example.factoryguard.application.port.out.result.LoadResultImagePort;
import com.example.factoryguard.application.port.out.result.ResultQueryPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
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
        lenient().when(securityUtils.isSiteAdmin()).thenReturn(false);
        lenient().when(securityUtils.requireOrganizationId()).thenReturn(10L);
    }

    @Test
    @DisplayName("결과 목록은 빈 페이지로도 응답할 수 있다")
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
        when(resultQueryPort.findPage(query, 10L)).thenReturn(emptyPage);

        ResultPageResponse result = service.execute(query);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("No.19 결과 목록 - decisionCode=DEFECT 필터가 그대로 Port로 전달된다")
    void listForwardsDecisionFilterToPort() {
        ListInspectionResultsQuery query = new ListInspectionResultsQuery(
                null, null, null, null, null, null,
                "DEFECT", null, null, 0, 20);
        when(resultQueryPort.findPage(any(), anyLong())).thenReturn(ResultPageResponse.builder()
                .content(List.of())
                .page(0).size(20).totalElements(0).totalPages(0)
                .summary(ResultListSummaryResponse.builder().totalCount(0).build())
                .build());

        ResultPageResponse response = service.execute(query);

        assertThat(response).isNotNull();
        assertThat(query.getDecision()).isEqualTo("DEFECT");
    }

    @Test
    @DisplayName("No.19 잘못된 decisionCode - 검증 실패")
    void invalidDecisionRejected() {
        ListInspectionResultsQuery query = new ListInspectionResultsQuery(
                null, null, null, null, null, null,
                "ABNORMAL", null, null, 0, 20);

        assertThatThrownBy(() -> service.execute(query))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("No.19 페이지 size 100 초과 - 검증 실패")
    void oversizedPageRejected() {
        ListInspectionResultsQuery query = new ListInspectionResultsQuery(
                null, null, null, null, null, null,
                null, null, null, 0, 200);

        assertThatThrownBy(() -> service.execute(query))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("No.20 결과 상세 - resultId 기준 점수/판정 반환")
    void detailReturnsScoreAndDecision() {
        long resultId = 3001L;
        when(resultQueryPort.findOrganizationIdByResultId(resultId)).thenReturn(Optional.of(10L));
        ResultDetailResponse base = ResultDetailResponse.builder()
                .resultId(resultId)
                .inspectionId(2001L)
                .result(ResultDecisionResponse.builder()
                        .decisionCode("DEFECT")
                        .finalDecisionCode("DEFECT")
                        .score(new BigDecimal("0.91"))
                        .confidence(new BigDecimal("0.88"))
                        .build())
                .build();
        when(resultQueryPort.findDetail(resultId)).thenReturn(Optional.of(base));

        ResultDetailResponse detail = service.execute(resultId);

        assertThat(detail.getResultId()).isEqualTo(resultId);
        assertThat(detail.getResult().getDecisionCode()).isEqualTo("DEFECT");
        assertThat(detail.getResult().getScore()).isEqualByComparingTo("0.91");
        assertThat(detail.getDescription()).isNotNull();
    }

    @Test
    @DisplayName("No.20 결과 상세 - 잘못된 resultId(0) 검증 실패")
    void detailRejectsInvalidId() {
        assertThatThrownBy(() -> service.execute(0L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }
}
