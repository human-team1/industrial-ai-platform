package com.example.factoryguard.adapter.in.web.result;

import com.example.factoryguard.adapter.in.web.result.mapper.ResultWebMapper;
import com.example.factoryguard.application.dto.result.AnomalyRegionResult;
import com.example.factoryguard.application.dto.result.ListInspectionResultsQuery;
import com.example.factoryguard.application.dto.result.ResultArtifactResult;
import com.example.factoryguard.application.dto.result.ResultDescriptionResponse;
import com.example.factoryguard.application.dto.result.ResultDetailResponse;
import com.example.factoryguard.application.dto.result.ResultImageResult;
import com.example.factoryguard.application.dto.result.ResultPageResponse;
import com.example.factoryguard.application.port.in.result.GetAnomalyRegionsUseCase;
import com.example.factoryguard.application.port.in.result.GetInspectionResultDetailUseCase;
import com.example.factoryguard.application.port.in.result.GetResultArtifactsUseCase;
import com.example.factoryguard.application.port.in.result.GetResultExplanationUseCase;
import com.example.factoryguard.application.port.in.result.GetResultImagesUseCase;
import com.example.factoryguard.application.port.in.result.ListInspectionResultsUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/results")
@RequiredArgsConstructor
public class ResultController {

    private final ListInspectionResultsUseCase listInspectionResultsUseCase;
    private final GetInspectionResultDetailUseCase getInspectionResultDetailUseCase;
    private final GetResultArtifactsUseCase getResultArtifactsUseCase;
    private final GetResultImagesUseCase getResultImagesUseCase;
    private final GetAnomalyRegionsUseCase getAnomalyRegionsUseCase;
    private final GetResultExplanationUseCase getResultExplanationUseCase;
    private final ResultWebMapper resultWebMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPageResponse>> listResults(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,
            @RequestParam(required = false) String decisionCode,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String equipmentName,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String runType,
            @RequestParam(required = false) String decision,
            @RequestParam(required = false) String resultStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ListInspectionResultsQuery query = resultWebMapper.toListQuery(
                startDate != null ? startDate : from,
                endDate != null ? endDate : to,
                keyword,
                equipmentName,
                productName,
                runType,
                decisionCode != null ? decisionCode : decision,
                resultStatus,
                targetId,
                page,
                size
        );
        ResultPageResponse response = listInspectionResultsUseCase.execute(query);
        return ResponseEntity.ok(ApiResponse.success(response, "검사 결과 목록을 조회했습니다."));
    }

    @GetMapping("/{resultId}")
    public ResponseEntity<ApiResponse<ResultDetailResponse>> getResultDetail(@PathVariable Long resultId) {
        ResultDetailResponse response = getInspectionResultDetailUseCase.execute(resultId);
        return ResponseEntity.ok(ApiResponse.success(response, "검사 결과 상세를 조회했습니다."));
    }

    @GetMapping("/{resultId}/artifacts")
    public ApiResponse<List<ResultArtifactResult>> getArtifacts(@PathVariable Long resultId) {
        return ApiResponse.success(getResultArtifactsUseCase.getArtifacts(resultId));
    }

    @GetMapping("/{resultId}/images")
    public ApiResponse<List<ResultImageResult>> getImages(@PathVariable Long resultId) {
        return ApiResponse.success(getResultImagesUseCase.getImages(resultId));
    }

    @GetMapping("/{resultId}/regions")
    public ApiResponse<List<AnomalyRegionResult>> getRegions(@PathVariable Long resultId) {
        return ApiResponse.success(getAnomalyRegionsUseCase.getRegions(resultId));
    }

    @GetMapping("/{resultId}/explanation")
    public ApiResponse<ResultDescriptionResponse> getExplanation(@PathVariable Long resultId) {
        return ApiResponse.success(getResultExplanationUseCase.getExplanation(resultId));
    }
}
