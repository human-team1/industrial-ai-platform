package com.example.factoryguard.application.service.result;

import com.example.factoryguard.application.dto.result.ListInspectionResultsQuery;
import com.example.factoryguard.application.dto.result.ResultChecklistItemResponse;
import com.example.factoryguard.application.dto.result.ResultDescriptionResponse;
import com.example.factoryguard.application.dto.result.ResultDetailResponse;
import com.example.factoryguard.application.dto.result.ResultPageResponse;
import com.example.factoryguard.application.port.in.result.GetInspectionResultDetailUseCase;
import com.example.factoryguard.application.port.in.result.ListInspectionResultsUseCase;
import com.example.factoryguard.application.port.out.result.ResultQueryPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResultQueryService implements ListInspectionResultsUseCase, GetInspectionResultDetailUseCase {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_DECISIONS = Set.of("NORMAL", "DEFECT", "RETEST");
    private static final Set<String> ALLOWED_RESULT_STATUSES = Set.of(
            "SUCCESS", "FAILED", "REVIEW_REQUIRED", "CORRECTED"
    );

    private final ResultQueryPort resultQueryPort;
    private final SecurityUtils securityUtils;

    @Override
    public ResultPageResponse execute(ListInspectionResultsQuery query) {
        validateQuery(query);
        Long organizationId = securityUtils.isSiteAdmin() ? null : securityUtils.requireOrganizationId();
        return resultQueryPort.findPage(query, organizationId);
    }

    @Override
    public ResultDetailResponse execute(Long resultId) {
        if (resultId == null || resultId < 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "resultId는 1 이상의 값이어야 합니다.");
        }

        Long resultOrganizationId = resultQueryPort.findOrganizationIdByResultId(resultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "검사 결과를 찾을 수 없습니다."));

        securityUtils.assertSameOrganization(resultOrganizationId, "조회 권한이 없는 검사 결과입니다.");

        ResultDetailResponse detail = resultQueryPort.findDetail(resultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "검사 결과를 찾을 수 없습니다."));
        return withMvpGuidance(detail);
    }

    private ResultDetailResponse withMvpGuidance(ResultDetailResponse detail) {
        String decisionCode = detail.getResult().getFinalDecisionCode() != null
                ? detail.getResult().getFinalDecisionCode()
                : detail.getResult().getDecisionCode();

        return ResultDetailResponse.builder()
                .resultId(detail.getResultId())
                .inspectionId(detail.getInspectionId())
                .target(detail.getTarget())
                .inspection(detail.getInspection())
                .result(detail.getResult())
                .artifacts(detail.getArtifacts())
                .images(detail.getImages())
                .review(detail.getReview())
                .description(buildDescription(decisionCode, detail.getResult().getFailureReason()))
                .checklist(buildChecklist(decisionCode))
                .eventLogs(detail.getEventLogs())
                .relatedResults(detail.getRelatedResults())
                .build();
    }

    private ResultDescriptionResponse buildDescription(String decisionCode, String failureReason) {
        if (failureReason != null && !failureReason.isBlank()) {
            return ResultDescriptionResponse.builder()
                    .summary("탐지 처리 중 오류가 발생했습니다. 실패 사유: " + failureReason)
                    .recommendedAction("입력 이미지와 설비 상태를 확인한 뒤 재탐지를 수행하세요.")
                    .build();
        }
        if ("DEFECT".equals(decisionCode)) {
            return ResultDescriptionResponse.builder()
                    .summary("설비 또는 제품 표면에서 비정상적인 마모, 손상 또는 오염 패턴이 감지되었습니다.")
                    .recommendedAction("설비 상태 확인 후 이상 부위를 우선 점검하세요.")
                    .build();
        }
        if ("RETEST".equals(decisionCode) || "RECHECK".equals(decisionCode) || "REINSPECTION".equals(decisionCode)) {
            return ResultDescriptionResponse.builder()
                    .summary("탐지 결과가 경계 구간에 있어 재검사가 필요합니다.")
                    .recommendedAction("동일 조건에서 이미지를 다시 수집하고 재탐지를 수행하세요.")
                    .build();
        }
        return ResultDescriptionResponse.builder()
                .summary("현재 탐지 결과에서 주요 이상 패턴은 확인되지 않았습니다.")
                .recommendedAction("정기 점검 주기에 따라 설비 상태를 계속 모니터링하세요.")
                .build();
    }

    private List<ResultChecklistItemResponse> buildChecklist(String decisionCode) {
        if ("DEFECT".equals(decisionCode)) {
            return List.of(
                    checklist("설비 상태 확인", "설비 진동, 소음, 온도 상태를 확인합니다.", "REQUIRED"),
                    checklist("해당 부위 점검", "탐지된 위치의 마모, 균열, 이물질 여부를 육안으로 점검합니다.", "REQUIRED"),
                    checklist("윤활 상태 확인", "윤활유 상태와 주입량을 확인하고 필요 시 보충 또는 교체합니다.", "RECOMMENDED"),
                    checklist("재탐지 수행", "조치 후 재검사를 수행하여 이상 여부를 재확인합니다.", "OPTIONAL")
            );
        }
        if ("RETEST".equals(decisionCode) || "RECHECK".equals(decisionCode) || "REINSPECTION".equals(decisionCode)) {
            return List.of(
                    checklist("촬영 조건 확인", "조명, 초점, 흔들림 등 이미지 수집 조건을 확인합니다.", "RECOMMENDED"),
                    checklist("재탐지 수행", "동일 설비와 품목 기준으로 재검사를 수행합니다.", "RECOMMENDED")
            );
        }
        return List.of(
                checklist("정기 점검 유지", "정상 판정이지만 정기 점검 주기를 유지합니다.", "OPTIONAL")
        );
    }

    private ResultChecklistItemResponse checklist(String title, String description, String priority) {
        return ResultChecklistItemResponse.builder()
                .title(title)
                .description(description)
                .priority(priority)
                .build();
    }

    private void validateQuery(ListInspectionResultsQuery query) {
        if (query.getPage() < 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "page는 0 이상이어야 합니다.");
        }
        if (query.getSize() < 1 || query.getSize() > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "size는 1 이상 100 이하이어야 합니다.");
        }
        if (query.getFrom() != null && query.getTo() != null && query.getFrom().isAfter(query.getTo())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "from은 to보다 늦을 수 없습니다.");
        }
        if (query.getDecision() != null && !ALLOWED_DECISIONS.contains(toUpper(query.getDecision()))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "허용되지 않은 판정 결과입니다.");
        }
        if (query.getResultStatus() != null && !ALLOWED_RESULT_STATUSES.contains(toUpper(query.getResultStatus()))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "허용되지 않은 결과 상태입니다.");
        }
    }

    private String toUpper(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
