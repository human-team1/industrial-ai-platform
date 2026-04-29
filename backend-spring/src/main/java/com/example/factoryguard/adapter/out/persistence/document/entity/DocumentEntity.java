package com.example.factoryguard.adapter.out.persistence.document.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DocumentEntity {

    private Long documentId;
    private Long organizationId;
    private Long ownerUserId;
    private String title;
    private String documentType;  // ✅ String으로 DB에 저장
    private String currentStatus; // ✅ String으로 DB에 저장
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}