package com.example.factoryguard.adapter.in.web.document;

import com.example.factoryguard.adapter.in.web.document.dto.UpdateDocumentMetadataBody;
import com.example.factoryguard.application.dto.document.CreateDocumentVersionCommand;
import com.example.factoryguard.application.dto.document.CreateDocumentWithFileCommand;
import com.example.factoryguard.application.dto.document.DocumentCreateResult;
import com.example.factoryguard.application.dto.document.DocumentDetailResult;
import com.example.factoryguard.application.dto.document.DocumentListPageResult;
import com.example.factoryguard.application.dto.document.DocumentSearchQuery;
import com.example.factoryguard.application.dto.document.DocumentSummaryResult;
import com.example.factoryguard.application.dto.document.UpdateDocumentMetadataCommand;
import com.example.factoryguard.application.port.in.document.DocumentCrudUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentCrudUseCase documentCrudUseCase;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<DocumentListPageResult>> listDocuments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String indexingStatus,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String equipmentType,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        DocumentSearchQuery query = DocumentSearchQuery.builder()
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
        return ResponseEntity.ok(ApiResponse.success(documentCrudUseCase.listDocuments(query), "문서 목록을 조회했습니다."));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DocumentSummaryResult>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(documentCrudUseCase.getSummary(), "문서 요약 통계를 조회했습니다."));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentDetailResult>> getDetail(@PathVariable Long documentId) {
        return ResponseEntity.ok(ApiResponse.success(documentCrudUseCase.getDetail(documentId), "문서 상세를 조회했습니다."));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<DocumentCreateResult>> createDocument(
            @RequestPart("file") MultipartFile file,
            @RequestPart("title") String title,
            @RequestPart(value = "category", required = false) String category,
            @RequestPart(value = "equipmentType", required = false) String equipmentType,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "tags", required = false) String tags
    ) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        DocumentCreateResult result = documentCrudUseCase.createDocument(CreateDocumentWithFileCommand.builder()
                .userId(principal.userId())
                .organizationId(principal.organizationId())
                .file(file)
                .title(title)
                .category(category)
                .equipmentType(equipmentType)
                .description(description)
                .tags(parseCsvTags(tags))
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "문서를 등록했습니다."));
    }

    @PatchMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentDetailResult>> updateMetadata(
            @PathVariable Long documentId,
            @RequestBody @Valid UpdateDocumentMetadataBody body
    ) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        DocumentDetailResult result = documentCrudUseCase.updateMetadata(UpdateDocumentMetadataCommand.builder()
                .userId(principal.userId())
                .organizationId(principal.organizationId())
                .documentId(documentId)
                .title(body.getTitle())
                .category(body.getCategory())
                .equipmentType(body.getEquipmentType())
                .description(body.getDescription())
                .tags(body.getTags())
                .build());
        return ResponseEntity.ok(ApiResponse.success(result, "문서 메타데이터를 수정했습니다."));
    }

    @PostMapping(value = "/{documentId}/versions", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<DocumentCreateResult>> addVersion(
            @PathVariable Long documentId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "changeReason", required = false) String changeReason
    ) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        DocumentCreateResult result = documentCrudUseCase.createVersion(CreateDocumentVersionCommand.builder()
                .userId(principal.userId())
                .organizationId(principal.organizationId())
                .documentId(documentId)
                .file(file)
                .changeReason(changeReason)
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "문서 버전을 추가했습니다."));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId) {
        documentCrudUseCase.softDelete(documentId);
        return ResponseEntity.noContent().build();
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
