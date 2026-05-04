package com.example.factoryguard.application.dto.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocumentIndexedChunkResponse {

    private Integer sequenceNo;
    private String content;
    private Integer pageNo;
    private String section;
    private String vectorRef;
}
