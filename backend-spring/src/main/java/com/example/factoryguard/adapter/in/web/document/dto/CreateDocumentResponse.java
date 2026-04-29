package com.example.factoryguard.adapter.in.web.document.dto;

import com.example.factoryguard.application.dto.document.DocumentDetail;
import com.example.factoryguard.domain.document.vo.DocumentStatus;
import com.example.factoryguard.domain.document.vo.DocumentType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateDocumentResponse {

    private Long documentId;
    private Long organizationId;
    private Long ownerUserId;
    private String title;
    private DocumentType documentType;
    private DocumentStatus currentStatus;

    public static CreateDocumentResponse from(DocumentDetail detail) {
        CreateDocumentResponse response = new CreateDocumentResponse();
        response.documentId = detail.getDocumentId();
        response.organizationId = detail.getOrganizationId();
        response.ownerUserId = detail.getOwnerUserId();
        response.title = detail.getTitle();
        response.documentType = detail.getDocumentType();
        response.currentStatus = detail.getCurrentStatus();
        return response;
    }
}