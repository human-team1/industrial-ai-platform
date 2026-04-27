package com.example.factoryguard.adapter.in.web.user;

import com.example.factoryguard.adapter.in.web.user.dto.UpdateThresholdRequest;
import com.example.factoryguard.application.dto.user.UpdateThresholdResult;
import com.example.factoryguard.application.dto.user.UserMeResult;
import com.example.factoryguard.application.dto.user.UserThresholdResult;
import com.example.factoryguard.application.port.in.user.GetMyProfileUseCase;
import com.example.factoryguard.application.port.in.user.GetMyThresholdsUseCase;
import com.example.factoryguard.application.port.in.user.UpdateMyThresholdUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final GetMyProfileUseCase getMyProfileUseCase;
    private final GetMyThresholdsUseCase getMyThresholdsUseCase;
    private final UpdateMyThresholdUseCase updateMyThresholdUseCase;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResult>> me() {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        UserMeResult result = getMyProfileUseCase.execute(p.userId(), p.sessionId());
        return ResponseEntity.ok(ApiResponse.success(result, "내 프로필입니다."));
    }

    @GetMapping("/me/thresholds")
    public ResponseEntity<ApiResponse<UserThresholdResult>> myThresholds() {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        UserThresholdResult result = getMyThresholdsUseCase.execute(p.userId(), p.sessionId());
        return ResponseEntity.ok(ApiResponse.success(result, "임계값 조회 성공."));
    }

    @PatchMapping("/me/thresholds/{thresholdId}")
    public ResponseEntity<ApiResponse<UpdateThresholdResult>> updateThreshold(
            @PathVariable Long thresholdId,
            @RequestBody UpdateThresholdRequest request) {
        AuthenticatedPrincipal p = securityUtils.getCurrentPrincipal();
        UpdateThresholdResult result = updateMyThresholdUseCase.execute(p.userId(), p.sessionId(), thresholdId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(result, "임계값 수정 성공."));
    }
}