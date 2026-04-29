package com.example.factoryguard.adapter.in.web.signuprequest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
public class SignupRequestBody {

    @NotBlank
    private String signupToken;

    @NotBlank
    @Pattern(regexp = "^[0-9]{8,15}$", message = "phone은 숫자 8~15자리여야 합니다.")
    private String phone;

    @NotNull
    private Long organizationId;
}
