package com.example.factoryguard.adapter.in.web;

import com.example.factoryguard.adapter.in.web.dto.SignupRequest;
import com.example.factoryguard.adapter.in.web.dto.SignupResponse;
import com.example.factoryguard.application.port.in.SignupUseCase;
import com.example.factoryguard.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class SignupController {

    private final SignupUseCase signupUseCase;

    public SignupController(SignupUseCase signupUseCase) {
        this.signupUseCase = signupUseCase;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@RequestBody SignupRequest request) {
        var result = signupUseCase.signup(request.toCommand());
        var response = SignupResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(response, "회원가입이 완료되었습니다. 관리자 승인을 기다려주세요."));
    }
}