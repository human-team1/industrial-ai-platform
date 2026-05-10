package com.example.factoryguard.adapter.in.web.model;

import com.example.factoryguard.adapter.in.web.model.dto.CreateModelRequest;
import com.example.factoryguard.adapter.in.web.model.dto.DeployModelVersionRequest;
import com.example.factoryguard.adapter.in.web.model.dto.ReasonRequest;
import com.example.factoryguard.adapter.in.web.model.dto.RollbackModelDeploymentRequest;
import com.example.factoryguard.application.dto.model.*;
import com.example.factoryguard.application.port.in.model.*;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.SecurityUtils;
import com.example.factoryguard.config.web.RequestIdFilter;
import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class ModelAdminController {

    private final ListModelsUseCase listModelsUseCase;
    private final CreateModelUseCase createModelUseCase;
    private final GetModelUseCase getModelUseCase;
    private final ListModelVersionsUseCase listModelVersionsUseCase;
    private final UploadModelVersionUseCase uploadModelVersionUseCase;
    private final GetModelVersionUseCase getModelVersionUseCase;
    private final ListModelArtifactsUseCase listModelArtifactsUseCase;
    private final ActivateModelVersionUseCase activateModelVersionUseCase;
    private final DeprecateModelVersionUseCase deprecateModelVersionUseCase;
    private final ListModelDeploymentsUseCase listModelDeploymentsUseCase;
    private final DeployModelVersionUseCase deployModelVersionUseCase;
    private final DeactivateModelDeploymentUseCase deactivateModelDeploymentUseCase;
    private final ActivateModelDeploymentUseCase activateModelDeploymentUseCase;
    private final DeleteModelDeploymentUseCase deleteModelDeploymentUseCase;
    private final RollbackModelDeploymentUseCase rollbackModelDeploymentUseCase;
    private final DeleteModelVersionUseCase deleteModelVersionUseCase;
    private final GenerateModelVersionFromNormalImagesUseCase generateModelVersionFromNormalImagesUseCase;
    private final SecurityUtils securityUtils;

    @GetMapping("/api/v1/models")
    public ResponseEntity<ApiResponse<ModelPageResponse<ModelSummaryResponse>>> listModels(
            @RequestParam(required = false) String modelType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                listModelsUseCase.listModels(ListModelsQuery.builder()
                        .modelType(modelType)
                        .page(page)
                        .size(size)
                        .sort(sort)
                        .build()),
                "모델 목록을 조회했습니다."
        ));
    }

    @PostMapping("/api/v1/models")
    public ResponseEntity<ApiResponse<ModelDetailResponse>> createModel(@RequestBody CreateModelRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                createModelUseCase.createModel(CreateModelCommand.builder()
                        .modelName(request.getModelName())
                        .modelType(request.getModelType())
                        .description(request.getDescription())
                        .build()),
                "모델을 등록했습니다."
        ));
    }

    @GetMapping("/api/v1/models/{modelId}")
    public ResponseEntity<ApiResponse<ModelDetailResponse>> getModel(@PathVariable Long modelId) {
        return ResponseEntity.ok(ApiResponse.success(getModelUseCase.getModel(modelId), "모델 상세를 조회했습니다."));
    }

    @GetMapping("/api/v1/models/{modelId}/versions")
    public ResponseEntity<ApiResponse<ModelPageResponse<ModelVersionSummaryResponse>>> listVersions(
            @PathVariable Long modelId,
            @RequestParam(required = false) String modelCategory,
            @RequestParam(required = false) String modelProfile,
            @RequestParam(required = false) String deployStatus,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                listModelVersionsUseCase.listModelVersions(ListModelVersionsQuery.builder()
                        .modelId(modelId)
                        .modelCategory(modelCategory)
                        .modelProfile(modelProfile)
                        .deployStatus(deployStatus)
                        .isActive(isActive)
                        .page(page)
                        .size(size)
                        .sort(sort)
                        .build()),
                "모델 버전 목록을 조회했습니다."
        ));
    }

    @PostMapping(value = "/api/v1/models/{modelId}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ModelVersionDetailResponse>> uploadVersion(
            @PathVariable Long modelId,
            @RequestPart("ckptFile") MultipartFile ckptFile,
            @RequestPart("configFile") MultipartFile configFile,
            @RequestPart("memoryBankFile") MultipartFile memoryBankFile,
            @RequestPart(value = "labelsFile", required = false) MultipartFile labelsFile,
            @RequestPart("versionName") String versionName,
            @RequestPart("modelCategory") String modelCategory,
            @RequestPart("modelProfile") String modelProfile,
            @RequestPart(value = "framework", required = false) String framework,
            @RequestPart(value = "inputSize", required = false) String inputSize,
            @RequestPart(value = "thresholdDefault", required = false) String thresholdDefault,
            @RequestPart(value = "accuracy", required = false) String accuracy,
            @RequestPart(value = "precisionScore", required = false) String precisionScore,
            @RequestPart(value = "recallScore", required = false) String recallScore,
            @RequestPart(value = "f1Score", required = false) String f1Score,
            @RequestPart(value = "aurocScore", required = false) String aurocScore
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                uploadModelVersionUseCase.uploadModelVersion(UploadModelVersionCommand.builder()
                        .modelId(modelId)
                        .ckptFile(ckptFile)
                        .configFile(configFile)
                        .memoryBankFile(memoryBankFile)
                        .labelsFile(labelsFile)
                        .versionName(versionName)
                        .modelCategory(parseModelCategory(modelCategory))
                        .modelProfile(parseModelProfile(modelProfile))
                        .framework(framework)
                        .inputSize(inputSize)
                        .thresholdDefault(parseDecimal(thresholdDefault))
                        .accuracy(parseDecimal(accuracy))
                        .precisionScore(parseDecimal(precisionScore))
                        .recallScore(parseDecimal(recallScore))
                        .f1Score(parseDecimal(f1Score))
                        .aurocScore(parseDecimal(aurocScore))
                        .build()),
                "모델 버전을 업로드했습니다."
        ));
    }

    @PostMapping(value = "/api/v1/models/{modelId}/versions/from-normal-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GenerateModelVersionsFromNormalImagesResponse>> generateFromNormalImages(
            @PathVariable Long modelId,
            @RequestPart("normalImages") List<MultipartFile> normalImages,
            @RequestPart("modelCategory") String modelCategory,
            @RequestPart(value = "modelProfile", required = false) String modelProfile,
            @RequestPart("organizationId") String organizationId,
            @RequestPart(value = "targetId", required = false) String targetId,
            @RequestPart("deploymentScope") String deploymentScope,
            @RequestPart(value = "versionName", required = false) String versionName,
            @RequestPart(value = "thresholdDefault", required = false) String thresholdDefault,
            @RequestPart(value = "reason", required = false) String reason
    ) {
        ModelProfile parsedProfile = modelProfile == null || modelProfile.trim().isEmpty()
                ? null
                : parseModelProfile(modelProfile);
        GenerateModelVersionsFromNormalImagesResponse response = generateModelVersionFromNormalImagesUseCase.generateFromNormalImages(
                GenerateModelVersionFromNormalImagesCommand.builder()
                        .modelId(modelId)
                        .organizationId(parseLong(organizationId, "organizationId"))
                        .targetId(parseNullableLong(targetId, "targetId"))
                        .deploymentScope(parseDeploymentScope(deploymentScope))
                        .modelCategory(parseModelCategory(modelCategory))
                        .modelProfile(parsedProfile)
                        .versionName(versionName)
                        .thresholdDefault(parseDecimal(thresholdDefault))
                        .reason(reason)
                        .normalImages(normalImages)
                        .actorUserId(securityUtils.getCurrentUserId())
                        .requestId(MDC.get(RequestIdFilter.MDC_KEY))
                        .build()
        );
        String message = parsedProfile == null
                ? "정상 이미지셋 기반 속도형/성능형 모델 버전이 생성되고 배포되었습니다."
                : "정상 이미지셋 기반 모델 버전이 생성되고 배포되었습니다.";
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @GetMapping("/api/v1/model-versions/{versionId}")
    public ResponseEntity<ApiResponse<ModelVersionDetailResponse>> getVersion(@PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.success(getModelVersionUseCase.getModelVersion(versionId), "모델 버전 상세를 조회했습니다."));
    }

    @GetMapping("/api/v1/model-versions/{versionId}/artifacts")
    public ResponseEntity<ApiResponse<List<ModelArtifactResponse>>> getArtifacts(@PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.success(listModelArtifactsUseCase.listModelArtifacts(versionId), "모델 artifact 목록을 조회했습니다."));
    }

    @PatchMapping("/api/v1/model-versions/{versionId}/activate")
    public ResponseEntity<ApiResponse<ModelVersionDetailResponse>> activate(@PathVariable Long versionId, @RequestBody(required = false) ReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                activateModelVersionUseCase.activateModelVersion(ModelVersionStatusCommand.builder()
                        .versionId(versionId)
                        .reason(request == null ? null : request.getReason())
                        .build()),
                "모델 버전을 활성화했습니다."
        ));
    }

    @PatchMapping("/api/v1/model-versions/{versionId}/deprecate")
    public ResponseEntity<ApiResponse<ModelVersionDetailResponse>> deprecate(@PathVariable Long versionId, @RequestBody(required = false) ReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                deprecateModelVersionUseCase.deprecateModelVersion(ModelVersionStatusCommand.builder()
                        .versionId(versionId)
                        .reason(request == null ? null : request.getReason())
                        .build()),
                "모델 버전을 사용 중단했습니다."
        ));
    }

    @DeleteMapping("/api/v1/model-versions/{versionId}")
    public ResponseEntity<ApiResponse<ModelVersionDetailResponse>> deleteVersion(@PathVariable Long versionId, @RequestBody(required = false) ReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                deleteModelVersionUseCase.deleteModelVersion(ModelVersionStatusCommand.builder()
                        .versionId(versionId)
                        .reason(request == null ? null : request.getReason())
                        .build()),
                "모델 버전이 삭제 처리되었습니다."
        ));
    }

    @GetMapping("/api/v1/model-deployments")
    public ResponseEntity<ApiResponse<ModelPageResponse<ModelDeploymentResponse>>> listDeployments(
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Long modelVersionId,
            @RequestParam(required = false) String deploymentScope,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") Boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                listModelDeploymentsUseCase.listModelDeployments(ListModelDeploymentsQuery.builder()
                        .organizationId(organizationId)
                        .targetId(targetId)
                        .modelVersionId(modelVersionId)
                        .deploymentScope(deploymentScope)
                        .isActive(isActive)
                        .includeDeleted(includeDeleted)
                        .page(page)
                        .size(size)
                        .sort(sort)
                        .build()),
                "모델 배포 목록을 조회했습니다."
        ));
    }

    @PostMapping("/api/v1/model-versions/{versionId}/deployments")
    public ResponseEntity<ApiResponse<ModelDeploymentResponse>> deploy(@PathVariable Long versionId, @RequestBody DeployModelVersionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                deployModelVersionUseCase.deployModelVersion(DeployModelVersionCommand.builder()
                        .versionId(versionId)
                        .organizationId(request.getOrganizationId())
                        .targetId(request.getTargetId())
                        .deploymentScope(parseDeploymentScope(request.getDeploymentScope()))
                        .reason(request.getReason())
                        .build()),
                "모델을 배포했습니다."
        ));
    }

    @PatchMapping("/api/v1/model-deployments/{deploymentId}/deactivate")
    public ResponseEntity<ApiResponse<ModelDeploymentResponse>> deactivateDeployment(@PathVariable Long deploymentId, @RequestBody(required = false) ReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                deactivateModelDeploymentUseCase.deactivateModelDeployment(DeactivateModelDeploymentCommand.builder()
                        .deploymentId(deploymentId)
                        .reason(request == null ? null : request.getReason())
                        .build()),
                "모델 배포를 비활성화했습니다."
        ));
    }

    @PatchMapping("/api/v1/model-deployments/{deploymentId}/activate")
    public ResponseEntity<ApiResponse<ModelDeploymentResponse>> activateDeployment(@PathVariable Long deploymentId, @RequestBody(required = false) ReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                activateModelDeploymentUseCase.activateModelDeployment(ActivateModelDeploymentCommand.builder()
                        .deploymentId(deploymentId)
                        .reason(request == null ? null : request.getReason())
                        .build()),
                "모델 배포가 활성화되었습니다."
        ));
    }

    @DeleteMapping("/api/v1/model-deployments/{deploymentId}")
    public ResponseEntity<ApiResponse<ModelDeploymentResponse>> deleteDeployment(@PathVariable Long deploymentId, @RequestBody(required = false) ReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                deleteModelDeploymentUseCase.deleteModelDeployment(DeactivateModelDeploymentCommand.builder()
                        .deploymentId(deploymentId)
                        .reason(request == null ? null : request.getReason())
                        .build()),
                "모델 배포가 삭제 처리되었습니다."
        ));
    }

    @PatchMapping("/api/v1/model-deployments/{deploymentId}/rollback")
    public ResponseEntity<ApiResponse<ModelDeploymentResponse>> rollback(@PathVariable Long deploymentId, @RequestBody RollbackModelDeploymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                rollbackModelDeploymentUseCase.rollbackModelDeployment(RollbackModelDeploymentCommand.builder()
                        .deploymentId(deploymentId)
                        .rollbackToDeploymentId(request.getRollbackToDeploymentId())
                        .reason(request.getReason())
                        .build()),
                "모델 롤백을 완료했습니다."
        ));
    }

    private ModelCategory parseModelCategory(String value) {
        try {
            return ModelCategory.valueOf(requiredText(value).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "지원하지 않는 modelCategory입니다.");
        }
    }

    private ModelProfile parseModelProfile(String value) {
        try {
            return ModelProfile.valueOf(requiredText(value).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "지원하지 않는 modelProfile입니다.");
        }
    }

    private DeploymentScope parseDeploymentScope(String value) {
        try {
            return DeploymentScope.valueOf(requiredText(value).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "지원하지 않는 deploymentScope입니다.");
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "숫자 형식이 올바르지 않습니다.");
        }
    }

    private Long parseLong(String value, String fieldName) {
        try {
            return Long.valueOf(requiredText(value));
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, fieldName + " 형식이 올바르지 않습니다.");
        }
    }

    private Long parseNullableLong(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return parseLong(value, fieldName);
    }

    private String requiredText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.MODEL_VALIDATION_FAILED, "필수 값이 누락되었습니다.");
        }
        return value.trim();
    }
}
