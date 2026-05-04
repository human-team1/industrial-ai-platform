package com.example.factoryguard.adapter.in.web.document;

import com.example.factoryguard.application.dto.document.DocumentIndexingStatusResult;
import com.example.factoryguard.application.dto.document.DocumentUploadCommand;
import com.example.factoryguard.application.dto.document.DocumentUploadResult;
import com.example.factoryguard.application.dto.document.RequestDocumentIndexingCommand;
import com.example.factoryguard.application.port.in.document.RequestDocumentIndexingUseCase;
import com.example.factoryguard.application.port.in.document.UploadDocumentUseCase;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import com.example.factoryguard.domain.document.vo.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final UploadDocumentUseCase uploadDocumentUseCase;
    private final RequestDocumentIndexingUseCase requestDocumentIndexingUseCase;
    private final SecurityUtils securityUtils;

    @PostMapping(value = "/api/v1/documents", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<DocumentUploadResult>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("title") String title,
            @RequestPart("documentType") String documentType,
            @RequestPart(value = "category", required = false) String category,
            @RequestPart(value = "equipmentType", required = false) String equipmentType,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "tags", required = false) String tags,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId
    ) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        requireAdmin(principal);
        DocumentUploadCommand command = DocumentUploadCommand.builder()
                .userId(principal.userId())
                .organizationId(principal.organizationId())
                .title(title)
                .documentType(parseDocumentType(documentType))
                .category(category)
                .equipmentType(equipmentType)
                .description(description)
                .tags(parseTags(tags))
                .originalFileName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .content(readBytes(file))
                .requestId(resolveRequestId(requestId))
                .build();
        DocumentUploadResult result = uploadDocumentUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "문서가 업로드되었고 인덱싱 작업이 시작되었습니다."));
    }

    @PostMapping("/api/v1/document-versions/{versionId}/index-jobs")
    public ResponseEntity<ApiResponse<DocumentIndexingStatusResult>> requestIndexing(
            @PathVariable Long versionId,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId
    ) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        requireAdmin(principal);
        DocumentIndexingStatusResult result = requestDocumentIndexingUseCase.execute(
                new RequestDocumentIndexingCommand(principal.userId(), versionId, resolveRequestId(requestId))
        );
        return ResponseEntity.ok(ApiResponse.success(result, "문서 인덱싱이 완료되었습니다."));
    }

    private DocumentType parseDocumentType(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "문서 유형은 필수입니다.");
        }
        try {
            return DocumentType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "지원하지 않는 문서 유형입니다.");
        }
    }

    private void requireAdmin(AuthenticatedPrincipal principal) {
        if (principal.role() == null || !"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "문서 인덱싱 작업은 관리자만 요청할 수 있습니다.");
        }
    }

    private List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .toList();
    }

    private String resolveRequestId(String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "업로드 파일을 읽을 수 없습니다.");
        }
    }
}
