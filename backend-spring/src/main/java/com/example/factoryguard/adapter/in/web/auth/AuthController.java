package com.example.factoryguard.adapter.in.web.auth;

import com.example.factoryguard.adapter.in.web.auth.dto.GoogleLoginRequest;
import com.example.factoryguard.application.dto.auth.AuthMeResult;
import com.example.factoryguard.application.dto.auth.AuthStatus;
import com.example.factoryguard.application.dto.auth.GoogleLoginCommand;
import com.example.factoryguard.application.dto.auth.GoogleLoginResult;
import com.example.factoryguard.application.dto.auth.RefreshTokenResult;
import com.example.factoryguard.application.port.in.auth.GoogleLoginUseCase;
import com.example.factoryguard.application.port.in.auth.LogoutUseCase;
import com.example.factoryguard.application.port.in.auth.RefreshTokenUseCase;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.JwtProperties;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleLoginUseCase googleLoginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final JwtProperties jwtProperties;
    private final SecurityUtils securityUtils;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<GoogleLoginResult>> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletResponse response) {

        GoogleLoginResult result = googleLoginUseCase.execute(
                new GoogleLoginCommand(request.getIdToken())
        );

        if (result.getUserStatus() == AuthStatus.ACTIVE) {
            ResponseCookie refreshCookie = buildRefreshCookie(
                    result.getRefreshToken(),
                    jwtProperties.getRefreshExpireDays() * 24 * 60 * 60L
            );
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }

        return ResponseEntity.ok(ApiResponse.success(result, resolveMessage(result)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResult>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshTokenResult result = refreshTokenUseCase.execute(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(result, "토큰이 재발급되었습니다."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        try {
            logoutUseCase.execute(securityUtils.getCurrentUserId());
        } catch (Exception ignored) {
        }
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString());
        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthMeResult>> me() {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        AuthMeResult result = AuthMeResult.builder()
                .userId(principal.userId())
                .role(principal.role())
                .organizationId(principal.organizationId())
                .build();
        return ResponseEntity.ok(ApiResponse.success(result, "현재 사용자 정보입니다."));
    }

    private ResponseCookie buildRefreshCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    private String resolveMessage(GoogleLoginResult result) {
        return switch (result.getUserStatus()) {
            case ACTIVE -> "Google 로그인이 완료되었습니다.";
            case NEW -> "신규 사용자입니다. 회원가입을 진행해주세요.";
            case PENDING -> "가입 승인 대기 중입니다.";
            case REJECTED -> "가입이 거절된 계정입니다.";
        };
    }
}