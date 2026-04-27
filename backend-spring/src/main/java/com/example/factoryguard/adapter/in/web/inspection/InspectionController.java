package com.example.factoryguard.adapter.in.web.inspection;

import com.example.factoryguard.adapter.in.web.inspection.dto.SubmitInspectionRequest;
import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.application.dto.inspection.InspectionStatusResponse;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionCommand;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.port.in.inspection.GetInspectionStatusUseCase;
import com.example.factoryguard.application.port.in.inspection.SubmitInspectionUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final GetInspectionStatusUseCase getInspectionStatusUseCase;
    private final SubmitInspectionUseCase submitInspectionUseCase;
    private final MinioStorageAdapter minioStorageAdapter;
    private final MinioProperties minioProperties;
    private final SecurityUtils securityUtils;

    @GetMapping("/status")
    public ApiResponse<InspectionStatusResponse> getStatus() {
        return ApiResponse.success(InspectionStatusResponse.from(getInspectionStatusUseCase.getStatus()));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<SubmitInspectionResult>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("targetId") String targetId,
            @RequestPart(value = "thresholdId", required = false) String thresholdId) throws Exception {

        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();

        String objectKey = "inspections/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        String bucket = minioProperties.getBucketInspectionArtifacts();

        minioStorageAdapter.upload(
                bucket, objectKey,
                file.getInputStream(), file.getSize(),
                file.getOriginalFilename(), file.getContentType(), ""
        );

        String fileUrl = minioStorageAdapter.createPresignedGetUrl(bucket, objectKey, Duration.ofHours(1));

        SubmitInspectionResult result = submitInspectionUseCase.execute(new SubmitInspectionCommand(
                p.userId(),
                p.sessionId(),
                Long.parseLong(targetId),
                thresholdId != null ? Long.parseLong(thresholdId) : null,
                fileUrl,
                file.getOriginalFilename(),
                file.getContentType()
        ));

        return ResponseEntity.ok(ApiResponse.success(result, "검사 요청이 완료되었습니다."));
    }
}
