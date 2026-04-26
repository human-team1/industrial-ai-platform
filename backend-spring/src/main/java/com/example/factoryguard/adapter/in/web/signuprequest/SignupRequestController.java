package com.example.factoryguard.adapter.in.web.signuprequest;

import com.example.factoryguard.adapter.in.web.signuprequest.dto.RejectSignupRequestBody;
import com.example.factoryguard.adapter.in.web.signuprequest.dto.SignupRequestBody;
import com.example.factoryguard.application.dto.auth.SignupRequestCommand;
import com.example.factoryguard.application.dto.auth.SignupRequestResult;
import com.example.factoryguard.application.dto.signup.SignupRequestSummary;
import com.example.factoryguard.application.port.in.auth.SignupRequestUseCase;
import com.example.factoryguard.application.port.in.signup.ApproveSignupUseCase;
import com.example.factoryguard.application.port.in.signup.GetSignupRequestsUseCase;
import com.example.factoryguard.application.port.in.signup.RejectSignupUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/signup-requests")
@RequiredArgsConstructor
public class SignupRequestController {

    private final SignupRequestUseCase signupRequestUseCase;
    private final GetSignupRequestsUseCase getSignupRequestsUseCase;
    private final ApproveSignupUseCase approveSignupUseCase;
    private final RejectSignupUseCase rejectSignupUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<SignupRequestResult>> createSignupRequest(
            @Valid @RequestBody SignupRequestBody body) {

        SignupRequestResult result = signupRequestUseCase.execute(
                SignupRequestCommand.builder()
                        .googleSub(body.getGoogleSub())
                        .email(body.getEmail())
                        .name(body.getName())
                        .picture(body.getPicture())
                        .build()
        );

        return ResponseEntity.ok(ApiResponse.success(result, "가입 신청이 완료되었습니다. 관리자 승인 후 이용 가능합니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SignupRequestSummary>>> getSignupRequests() {
        return ResponseEntity.ok(ApiResponse.success(
                getSignupRequestsUseCase.execute(), "가입 신청 목록을 조회했습니다."));
    }

    @PatchMapping("/{requestId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long requestId) {
        approveSignupUseCase.execute(requestId, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "가입 승인이 완료되었습니다."));
    }

    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long requestId,
            @RequestBody(required = false) RejectSignupRequestBody body) {

        String reason = body != null ? body.getRejectReason() : null;
        rejectSignupUseCase.execute(requestId, currentUserId(), reason);
        return ResponseEntity.ok(ApiResponse.success(null, "가입 거절이 완료되었습니다."));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getName());
    }
}
