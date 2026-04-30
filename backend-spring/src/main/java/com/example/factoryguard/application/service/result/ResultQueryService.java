package com.example.factoryguard.application.service.result;

import com.example.factoryguard.application.dto.result.AnomalyRegionResult;
import com.example.factoryguard.application.dto.result.ListInspectionResultsQuery;
import com.example.factoryguard.application.dto.result.ResultArtifactResult;
import com.example.factoryguard.application.dto.result.ResultChecklistItemResponse;
import com.example.factoryguard.application.dto.result.ResultDescriptionResponse;
import com.example.factoryguard.application.dto.result.ResultDetailResponse;
import com.example.factoryguard.application.dto.result.ResultImageResult;
import com.example.factoryguard.application.dto.result.ResultPageResponse;
import com.example.factoryguard.application.port.in.result.GetAnomalyRegionsUseCase;
import com.example.factoryguard.application.port.in.result.GetInspectionResultDetailUseCase;
import com.example.factoryguard.application.port.in.result.GetResultExplanationUseCase;
import com.example.factoryguard.application.port.in.result.GetResultArtifactsUseCase;
import com.example.factoryguard.application.port.in.result.GetResultImagesUseCase;
import com.example.factoryguard.application.port.in.result.ListInspectionResultsUseCase;
import com.example.factoryguard.application.port.out.result.LoadAnomalyRegionPort;
import com.example.factoryguard.application.port.out.result.LoadResultArtifactPort;
import com.example.factoryguard.application.port.out.result.LoadResultImagePort;
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
public class ResultQueryService implements
        ListInspectionResultsUseCase,
        GetInspectionResultDetailUseCase,
        GetResultArtifactsUseCase,
        GetResultImagesUseCase,
        GetAnomalyRegionsUseCase,
        GetResultExplanationUseCase {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_DECISIONS = Set.of("NORMAL", "DEFECT", "RECHECK");
    private static final Set<String> ALLOWED_RESULT_STATUSES = Set.of(
            "SUCCESS", "FAILED", "REVIEW_REQUIRED", "CORRECTED"
    );

    private final ResultQueryPort resultQueryPort;
    private final LoadResultArtifactPort loadResultArtifactPort;
    private final LoadResultImagePort loadResultImagePort;
    private final LoadAnomalyRegionPort loadAnomalyRegionPort;
    private final SecurityUtils securityUtils;

    @Override
    public ResultPageResponse execute(ListInspectionResultsQuery query) {
        validateQuery(query);
        Long organizationId = securityUtils.isSiteAdmin() ? null : securityUtils.requireOrganizationId();
        return resultQueryPort.findPage(query, organizationId);
    }

    @Override
    public ResultDetailResponse execute(Long resultId) {
        validateResultId(resultId);
        assertResultReadable(resultId);
        ResultDetailResponse detail = resultQueryPort.findDetail(resultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "검사 결과를 찾을 수 없습니다."));
        return withMvpGuidance(detail);
    }

    @Override
    public List<ResultArtifactResult> getArtifacts(Long resultId) {
        validateResultId(resultId);
        assertResultReadable(resultId);
        return loadResultArtifactPort.findArtifactsByResultId(resultId).stream()
                .map(artifact -> ResultArtifactResult.builder()
                        .artifactId(artifact.getArtifactId())
                        .resultId(artifact.getResultId())
                        .artifactType(artifact.getArtifactType())
                        .fileId(artifact.getFileId())
                        .build())
                .toList();
    }

    @Override
    public List<ResultImageResult> getImages(Long resultId) {
        validateResultId(resultId);
        assertResultReadable(resultId);
        return loadResultImagePort.findImagesByResultId(resultId).stream()
                .map(image -> ResultImageResult.builder()
                        .imageId(image.getImageId())
                        .resultId(image.getResultId())
                        .fileId(image.getFileId())
                        .imageRole(image.getImageRole())
                        .build())
                .toList();
    }

    @Override
    public List<AnomalyRegionResult> getRegions(Long resultId) {
        validateResultId(resultId);
        assertResultReadable(resultId);
        return loadResultImagePort.findImagesByResultId(resultId).stream()
                .flatMap(image -> loadAnomalyRegionPort.findAllByImageId(image.getImageId()).stream())
                .map(region -> AnomalyRegionResult.builder()
                        .regionId(region.getRegionId())
                        .imageId(region.getImageId())
                        .labelCode(region.getLabelCode())
                        .bboxX(region.getBboxX())
                        .bboxY(region.getBboxY())
                        .bboxW(region.getBboxW())
                        .bboxH(region.getBboxH())
                        .score(region.getScore())
                        .build())
                .toList();
    }

    @Override
    public ResultDescriptionResponse getExplanation(Long resultId) {
        validateResultId(resultId);
        assertResultReadable(resultId);
        return resultQueryPort.findDetail(resultId)
                .map(this::withMvpGuidance)
                .map(ResultDetailResponse::getDescription)
                .orElseGet(() -> ResultDescriptionResponse.builder()
                        .summary("아직 생성된 설명이 없습니다.")
                        .recommendedAction("AI/RAG 연동 이후 설명이 생성됩니다.")
                        .build());
    }

    private void assertResultReadable(Long resultId) {
        Long resultOrganizationId = resultQueryPort.findOrganizationIdByResultId(resultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "검사 결과를 찾을 수 없습니다."));
        securityUtils.assertSameOrganization(resultOrganizationId, "조회 권한이 없는 검사 결과입니다.");
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
                    .summary("검사 처리 중 오류가 발생했습니다. 실패 사유: " + failureReason)
                    .recommendedAction("입력 파일과 설비 상태를 확인한 뒤 재검사를 요청하세요.")
                    .build();
        }
        if ("DEFECT".equals(decisionCode)) {
            return ResultDescriptionResponse.builder()
                    .summary("검사 결과 불량 징후가 감지되었습니다.")
                    .recommendedAction("대상 설비 또는 품목을 확인하고 필요 시 관리자 재검토를 요청하세요.")
                    .build();
        }
        if ("RECHECK".equals(decisionCode)) {
            return ResultDescriptionResponse.builder()
                    .summary("검사 결과가 경계 구간에 있어 재검사가 필요합니다.")
                    .recommendedAction("동일 조건에서 파일을 다시 수집하고 재검사를 수행하세요.")
                    .build();
        }
        if ("NORMAL".equals(decisionCode)) {
            return ResultDescriptionResponse.builder()
                    .summary("현재 검사 결과에서 주요 이상 징후는 확인되지 않았습니다.")
                    .recommendedAction("정기 점검 주기에 따라 설비 상태를 계속 모니터링하세요.")
                    .build();
        }
        return ResultDescriptionResponse.builder()
                .summary("아직 생성된 설명이 없습니다.")
                .recommendedAction("AI/RAG 연동 이후 설명이 생성됩니다.")
                .build();
    }

    private List<ResultChecklistItemResponse> buildChecklist(String decisionCode) {
        if ("DEFECT".equals(decisionCode)) {
            return List.of(
                    checklist("설비 상태 확인", "진동, 소음, 온도 등 설비 상태를 확인합니다.", "REQUIRED"),
                    checklist("대상 부위 점검", "감지된 부위의 마모, 균열, 오염 여부를 확인합니다.", "REQUIRED"),
                    checklist("재검사 수행", "조치 후 재검사를 수행해 이상 여부를 재확인합니다.", "OPTIONAL")
            );
        }
        if ("RECHECK".equals(decisionCode)) {
            return List.of(
                    checklist("촬영 조건 확인", "조명, 초점, 흔들림 등 파일 수집 조건을 확인합니다.", "RECOMMENDED"),
                    checklist("재검사 수행", "동일 기준으로 재검사를 수행합니다.", "RECOMMENDED")
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
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "startDate는 endDate보다 늦을 수 없습니다.");
        }
        if (query.getDecision() != null && !ALLOWED_DECISIONS.contains(toUpper(query.getDecision()))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "허용되지 않는 decisionCode입니다.");
        }
        if (query.getResultStatus() != null && !ALLOWED_RESULT_STATUSES.contains(toUpper(query.getResultStatus()))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "허용되지 않는 결과 상태입니다.");
        }
    }

    private void validateResultId(Long resultId) {
        if (resultId == null || resultId < 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "resultId는 1 이상이어야 합니다.");
        }
    }

    private String toUpper(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
