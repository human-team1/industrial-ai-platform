package com.example.factoryguard.adapter.out.persistence.document.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DocumentVersionEntity {

    private Long documentVersionId;
    private Long documentId;
    private Integer versionNo;
    private Long fileId;
    private String fileHash;
    private String indexingStatus; // ✅ String으로 DB에 저장
    private Integer indexedChunkCount;
    private String indexErrorMessage;
    private LocalDateTime indexedAt;
    private LocalDateTime createdAt;
}