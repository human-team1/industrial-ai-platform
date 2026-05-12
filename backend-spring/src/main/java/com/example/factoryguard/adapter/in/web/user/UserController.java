package com.example.factoryguard.adapter.in.web.user;

import com.example.factoryguard.adapter.in.web.user.dto.CreateMyThresholdRequest;
import com.example.factoryguard.adapter.in.web.user.dto.PatchMyProfileRequest;
import com.example.factoryguard.adapter.in.web.user.dto.PatchMySettingRequest;
import com.example.factoryguard.adapter.in.web.user.dto.UpdateThresholdRequest;
import com.example.factoryguard.application.dto.user.UserMeResult;
import com.example.factoryguard.application.dto.user.UserSettingResult;
import com.example.factoryguard.application.dto.user.UserThresholdResult;
import com.example.factoryguard.application.port.in.user.CreateMyThresholdUseCase;
import com.example.factoryguard.application.port.in.user.GetMyProfileUseCase;
import com.example.factoryguard.application.port.in.user.GetMyThresholdsUseCase;
import com.example.factoryguard.application.port.in.user.GetUserSettingUseCase;
import com.example.factoryguard.application.port.in.user.UpdateMyProfileUseCase;
import com.example.factoryguard.application.port.in.user.UpdateMyThresholdUseCase;
import com.example.factoryguard.application.port.in.user.UpdateUserSettingUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final GetMyProfileUseCase getMyProfileUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;
    private final GetMyThresholdsUseCase getMyThresholdsUseCase;
    private final CreateMyThresholdUseCase createMyThresholdUseCase;
    private final UpdateMyThresholdUseCase updateMyThresholdUseCase;
    private final GetUserSettingUseCase getUserSettingUseCase;
    private final UpdateUserSettingUseCase updateUserSettingUseCase;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResult>> me() {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        UserMeResult result = getMyProfileUseCase.execute(p.userId(), p.sessionId());
        return ResponseEntity.ok(ApiResponse.success(result, "내 프로필입니다."));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResult>> updateMyProfile(
            @RequestBody PatchMyProfileRequest request) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        UserMeResult result = updateMyProfileUseCase.execute(
                request.toCommand(p.userId(), p.sessionId())
        );
        return ResponseEntity.ok(ApiResponse.success(result, "내 정보를 저장했습니다."));
    }

    @GetMapping("/me/settings")
    public ResponseEntity<ApiResponse<UserSettingResult>> mySettings() {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        UserSettingResult result = getUserSettingUseCase.execute(p.userId(), p.sessionId());
        return ResponseEntity.ok(ApiResponse.success(result, "사용자 설정을 조회했습니다."));
    }

    @PatchMapping("/me/settings")
    public ResponseEntity<ApiResponse<UserSettingResult>> updateMySettings(
            @RequestBody PatchMySettingRequest request) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        UserSettingResult result = updateUserSettingUseCase.execute(
                request.toCommand(p.userId(), p.sessionId())
        );
        return ResponseEntity.ok(ApiResponse.success(result, "사용자 설정을 저장했습니다."));
    }

    @GetMapping("/me/thresholds")
    public ResponseEntity<ApiResponse<UserThresholdResult>> myThresholds() {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        UserThresholdResult result = getMyThresholdsUseCase.execute(p.userId(), p.sessionId());
        return ResponseEntity.ok(ApiResponse.success(result, "임계값 조회 성공."));
    }

    @PostMapping("/me/thresholds")
    public ResponseEntity<ApiResponse<UserThresholdResult>> createMyThreshold(
            @Valid @RequestBody CreateMyThresholdRequest request) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        UserThresholdResult result = createMyThresholdUseCase.execute(
                p.userId(), p.sessionId(), request.toCommand()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "사용자 임계값을 생성했습니다."));
    }

    @PatchMapping("/me/thresholds/{thresholdId}")
    public ResponseEntity<ApiResponse<UserThresholdResult>> updateThreshold(
            @PathVariable Long thresholdId,
            @Valid @RequestBody UpdateThresholdRequest request) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        UserThresholdResult result = updateMyThresholdUseCase.execute(
                p.userId(), p.sessionId(), thresholdId, request.toCommand()
        );
        return ResponseEntity.ok(ApiResponse.success(result, "사용자 임계값을 저장했습니다."));
    }
}
