package com.example.factoryguard.application.dto.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DocumentDeindexResponse {

    private Long documentId;
    private Long documentVersionId;
    private Long organizationId;
    private String collectionName;
    private Integer deletedVectorCount;
    private LocalDateTime deindexedAt;
}
