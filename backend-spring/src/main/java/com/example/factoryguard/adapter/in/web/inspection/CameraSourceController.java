package com.example.factoryguard.adapter.in.web.inspection;

import com.example.factoryguard.adapter.in.web.inspection.dto.CameraSourceResponse;
import com.example.factoryguard.adapter.in.web.inspection.dto.CreateCameraSourceRequest;
import com.example.factoryguard.adapter.in.web.inspection.dto.UpdateCameraSourceRequest;
import com.example.factoryguard.application.dto.inspection.CreateCameraSourceCommand;
import com.example.factoryguard.application.dto.inspection.UpdateCameraSourceCommand;
import com.example.factoryguard.application.port.in.inspection.CreateCameraSourceUseCase;
import com.example.factoryguard.application.port.in.inspection.DeleteCameraSourceUseCase;
import com.example.factoryguard.application.port.in.inspection.GetCameraSourcesUseCase;
import com.example.factoryguard.application.port.in.inspection.UpdateCameraSourceUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/camera-sources")
@RequiredArgsConstructor
public class CameraSourceController {

    private final CreateCameraSourceUseCase createCameraSourceUseCase;
    private final GetCameraSourcesUseCase getCameraSourcesUseCase;
    private final UpdateCameraSourceUseCase updateCameraSourceUseCase;
    private final DeleteCameraSourceUseCase deleteCameraSourceUseCase;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ApiResponse<List<CameraSourceResponse>> list() {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        return ApiResponse.success(getCameraSourcesUseCase.execute(p.organizationId()).stream()
                .map(CameraSourceResponse::from)
                .toList());
    }

    @PostMapping
    public ApiResponse<CameraSourceResponse> create(@RequestBody CreateCameraSourceRequest request) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        return ApiResponse.success(CameraSourceResponse.from(createCameraSourceUseCase.execute(
                new CreateCameraSourceCommand(p.userId(), p.organizationId(),
                        request.cameraName(), request.streamUrl()))),
                "카메라가 등록되었습니다.");
    }

    @PatchMapping("/{cameraId}")
    public ApiResponse<CameraSourceResponse> update(@PathVariable Long cameraId,
                                                    @RequestBody UpdateCameraSourceRequest request) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        return ApiResponse.success(CameraSourceResponse.from(updateCameraSourceUseCase.execute(
                new UpdateCameraSourceCommand(cameraId, p.organizationId(),
                        request.cameraName(), request.streamUrl(), request.status()))),
                "카메라가 수정되었습니다.");
    }

    @DeleteMapping("/{cameraId}")
    public ApiResponse<Void> delete(@PathVariable Long cameraId) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        deleteCameraSourceUseCase.execute(p.organizationId(), cameraId);
        return ApiResponse.success(null, "카메라가 삭제되었습니다.");
    }
}
