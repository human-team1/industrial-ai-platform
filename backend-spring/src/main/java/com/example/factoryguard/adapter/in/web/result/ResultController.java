package com.example.factoryguard.adapter.in.web.result;

import com.example.factoryguard.application.dto.result.ListInspectionResultsQuery;
import com.example.factoryguard.application.dto.result.ResultDetailResponse;
import com.example.factoryguard.application.dto.result.ResultPageResponse;
import com.example.factoryguard.application.port.in.result.GetInspectionResultDetailUseCase;
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

@RestController
@RequestMapping("/api/v1/results")
@RequiredArgsConstructor
public class ResultController {

    private final ListInspectionResultsUseCase listInspectionResultsUseCase;
    private final GetInspectionResultDetailUseCase getInspectionResultDetailUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPageResponse>> listResults(
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
        ListInspectionResultsQuery query = new ListInspectionResultsQuery(
                from,
                to,
                keyword,
                equipmentName,
                productName,
                runType,
                decision,
                resultStatus,
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
}
