package com.example.factoryguard.adapter.in.web.dto;

import com.example.factoryguard.application.port.in.dto.SignupCommand;
import javax.validation.constraints.Email;    
import javax.validation.constraints.NotBlank; 

public class SignupRequest {

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private final String email;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private final String name;

    @NotBlank(message = "Google 인증 정보(sub)는 필수입니다.")
    private final String googleSub;

    private final String picture;

    @NotBlank(message = "소속(회사) 정보는 필수 입력 항목입니다.")
    private final String company;

    private final String position;

    @NotBlank(message = "연락처는 필수 입력 항목입니다.")
    private final String phone;

    public SignupRequest(String email, String name, String googleSub, String picture,
                         String company, String position, String phone) {
        this.email = email;
        this.name = name;
        this.googleSub = googleSub;
        this.picture = picture;
        this.company = company;
        this.position = position;
        this.phone = phone;
    }

    public SignupCommand toCommand() {
        return new SignupCommand(
            this.email,
            null,
            this.name,
            this.googleSub,
            this.picture,
            this.company,
            this.position,
            this.phone
        );
    }

    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getGoogleSub() { return googleSub; }
    public String getPicture() { return picture; }
    public String getCompany() { return company; }
    public String getPosition() { return position; }
    public String getPhone() { return phone; }
}