package com.example.factoryguard.adapter.in.web.document.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class UpdateDocumentMetadataBody {

    @NotBlank
    private String title;
    /** DB에 컬럼이 없어 저장되지 않습니다. API 호환용 선택 필드입니다. */
    private String category;
    private String equipmentType;
    private String description;
    private String tags;
}
