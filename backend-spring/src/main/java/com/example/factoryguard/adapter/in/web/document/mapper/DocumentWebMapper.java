package com.example.factoryguard.adapter.in.web.document.mapper;

import com.example.factoryguard.adapter.in.web.document.dto.UpdateDocumentMetadataBody;
import com.example.factoryguard.application.dto.document.CreateDocumentVersionCommand;
import com.example.factoryguard.application.dto.document.CreateDocumentWithFileCommand;
import com.example.factoryguard.application.dto.document.DocumentSearchQuery;
import com.example.factoryguard.application.dto.document.UpdateDocumentMetadataCommand;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DocumentWebMapper {

    public DocumentSearchQuery toSearchQuery(
            String keyword,
            String documentType,
            String indexingStatus,
            String category,
            String equipmentType,
            String author,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        return DocumentSearchQuery.builder()
                .keyword(keyword)
                .documentType(documentType)
                .indexingStatus(indexingStatus)
                .category(category)
                .equipmentType(equipmentType)
                .author(author)
                .startDate(startDate)
                .endDate(endDate)
                .page(page)
                .size(size)
                .build();
    }

    public CreateDocumentWithFileCommand toCreateDocumentCommand(
            AuthenticatedPrincipal principal,
            MultipartFile file,
            String title,
            String category,
            String equipmentType,
            String description,
            String tags
    ) {
        return CreateDocumentWithFileCommand.builder()
                .userId(principal.userId())
                .organizationId(principal.organizationId())
                .file(file)
                .title(title)
                .category(category)
                .equipmentType(equipmentType)
                .description(description)
                .tags(parseCsvTags(tags))
                .build();
    }

    public UpdateDocumentMetadataCommand toUpdateMetadataCommand(
            AuthenticatedPrincipal principal,
            Long documentId,
            UpdateDocumentMetadataBody body
    ) {
        return UpdateDocumentMetadataCommand.builder()
                .userId(principal.userId())
                .organizationId(principal.organizationId())
                .documentId(documentId)
                .title(body.getTitle())
                .category(body.getCategory())
                .equipmentType(body.getEquipmentType())
                .description(body.getDescription())
                .tags(body.getTags())
                .build();
    }

    public CreateDocumentVersionCommand toCreateVersionCommand(
            AuthenticatedPrincipal principal,
            Long documentId,
            MultipartFile file,
            String changeReason
    ) {
        return CreateDocumentVersionCommand.builder()
                .userId(principal.userId())
                .organizationId(principal.organizationId())
                .documentId(documentId)
                .file(file)
                .changeReason(changeReason)
                .build();
    }

    private List<String> parseCsvTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
