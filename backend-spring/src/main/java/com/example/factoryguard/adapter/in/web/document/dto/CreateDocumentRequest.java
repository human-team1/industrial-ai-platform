package com.example.factoryguard.adapter.in.web.document.dto;

import com.example.factoryguard.domain.document.vo.DocumentType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank; // ✅ javax로 수정
import javax.validation.constraints.NotNull;  // ✅ javax로 수정

@Getter
@NoArgsConstructor
public class CreateDocumentRequest {

    @NotBlank(message = "문서 제목은 필수입니다.")
    private String title;

    @NotNull(message = "문서 유형은 필수입니다.")
    private DocumentType documentType;

    @NotNull(message = "파일 ID는 필수입니다.")
    private Long fileId;

    @NotNull(message = "조직 ID는 필수입니다.")
    private Long organizationId;
}