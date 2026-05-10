package com.example.factoryguard.adapter.in.web.signuprequest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
public class SignupRequestBody {

    @NotBlank(message = "signupToken은 필수입니다.")
    private String signupToken;

    @NotBlank(message = "휴대전화 번호는 필수입니다.")
    @Pattern(regexp = "^[0-9]{8,15}$", message = "휴대전화 번호는 숫자 8~15자리여야 합니다.")
    private String phone;

    @NotNull(message = "조직 ID는 필수입니다.")
    private Long organizationId;
}
