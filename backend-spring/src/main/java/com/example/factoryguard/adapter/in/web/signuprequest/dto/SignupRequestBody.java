package com.example.factoryguard.adapter.in.web.signuprequest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class SignupRequestBody {

    @NotBlank
    private String googleSub;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String name;

    private String picture;
}
