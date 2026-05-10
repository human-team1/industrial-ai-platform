package com.example.factoryguard.adapter.in.web.signuprequest;

import com.example.factoryguard.adapter.in.web.signuprequest.dto.ApproveSignupBody;
import com.example.factoryguard.adapter.in.web.signuprequest.dto.PublicOrganizationResponse;
import com.example.factoryguard.adapter.in.web.signuprequest.dto.RejectSignupRequestBody;
import com.example.factoryguard.adapter.in.web.signuprequest.dto.SignupRequestBody;
import com.example.factoryguard.application.dto.auth.SignupRequestCommand;
import com.example.factoryguard.application.dto.auth.SignupRequestResult;
import com.example.factoryguard.application.dto.signup.SignupRequestSummary;
import com.example.factoryguard.application.port.in.auth.SignupRequestUseCase;
import com.example.factoryguard.application.port.in.organization.GetPublicOrganizationsUseCase;
import com.example.factoryguard.application.port.in.signup.ApproveSignupUseCase;
import com.example.factoryguard.application.port.in.signup.GetSignupRequestsUseCase;
import com.example.factoryguard.application.port.in.signup.RejectSignupUseCase;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.JwtTokenProvider;
import com.example.factoryguard.config.security.SecurityUtils;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/signup-requests")
@RequiredArgsConstructor
public class SignupRequestController {

    private final SignupRequestUseCase signupRequestUseCase;
    private final GetSignupRequestsUseCase getSignupRequestsUseCase;
    private final ApproveSignupUseCase approveSignupUseCase;
    private final RejectSignupUseCase rejectSignupUseCase;
    private final GetPublicOrganizationsUseCase getPublicOrganizationsUseCase;
    private final SecurityUtils securityUtils;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<SignupRequestResult>> createSignupRequest(
            @Valid @RequestBody SignupRequestBody body) {

        if (!jwtTokenProvider.validateToken(body.getSignupToken())) {
            throw new BusinessException(ErrorCode.INVALID_SIGNUP_TOKEN);
        }

        String googleSub;
        String email;
        String name;
        String picture;
        try {
            googleSub = jwtTokenProvider.extractSignupGoogleSub(body.getSignupToken());
            email = jwtTokenProvider.extractSignupEmail(body.getSignupToken());
            name = jwtTokenProvider.extractSignupName(body.getSignupToken());
            picture = jwtTokenProvider.extractSignupPicture(body.getSignupToken());
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.INVALID_SIGNUP_TOKEN);
        }

        SignupRequestResult result = signupRequestUseCase.execute(
                SignupRequestCommand.builder()
                        .googleSub(googleSub)
                        .email(email)
                        .name(name)
                        .picture(picture)
                        .phone(body.getPhone())
                        .organizationId(body.getOrganizationId())
                        .build()
        );

        return ResponseEntity.ok(ApiResponse.success(result, "가입 신청이 완료되었습니다. 관리자 승인 후 이용 가능합니다."));
    }

    @GetMapping("/organizations/public")
    public ResponseEntity<ApiResponse<List<PublicOrganizationResponse>>> getPublicOrganizations() {
        List<PublicOrganizationResponse> data = getPublicOrganizationsUseCase.execute().stream()
                .map(PublicOrganizationResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(data, "공개 조직 목록을 조회했습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SignupRequestSummary>>> getSignupRequests() {
        return ResponseEntity.ok(ApiResponse.success(
                getSignupRequestsUseCase.execute(), "가입 신청 목록을 조회했습니다."));
    }

    @PatchMapping("/{requestId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable Long requestId,
            @RequestBody(required = false) ApproveSignupBody body) {

        Long organizationId = body != null ? body.getOrganizationId() : null;
        approveSignupUseCase.execute(requestId, securityUtils.getCurrentUserId(), organizationId);
        return ResponseEntity.ok(ApiResponse.success(null, "가입 승인이 완료되었습니다."));
    }

    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long requestId,
            @RequestBody(required = false) RejectSignupRequestBody body) {

        String reason = body != null ? body.getRejectReason() : null;
        rejectSignupUseCase.execute(requestId, securityUtils.getCurrentUserId(), reason);
        return ResponseEntity.ok(ApiResponse.success(null, "가입 거절이 완료되었습니다."));
    }
}