package com.example.factoryguard.adapter.in.web;

import com.example.factoryguard.adapter.in.web.dto.SignupRequest;
import com.example.factoryguard.adapter.in.web.dto.SignupResponse;
import com.example.factoryguard.application.port.in.SignupUseCase;
import com.example.factoryguard.application.port.in.dto.SignupResult;
import com.example.factoryguard.common.response.ApiResponse; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class SignupController {

    private final SignupUseCase signupUseCase;

    public SignupController(SignupUseCase signupUseCase) {
        this.signupUseCase = signupUseCase;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResult result = signupUseCase.signup(request.toCommand());
        SignupResponse response = SignupResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "회원가입 신청이 완료되었습니다. 관리자 승인 후 이용 가능합니다.")); // ✅ ok() → success()
    }
}