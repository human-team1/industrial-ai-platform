package com.example.factoryguard.adapter.in.web.document.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class UpdateDocumentMetadataBody {

    @NotBlank
    private String title;

    private String category;
    private String equipmentType;
    private String description;
    private List<String> tags;
}
