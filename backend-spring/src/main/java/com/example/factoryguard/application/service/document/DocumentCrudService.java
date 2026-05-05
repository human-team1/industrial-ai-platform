package com.example.factoryguard.application.service.document;

import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.application.dto.document.CreateDocumentVersionCommand;
import com.example.factoryguard.application.dto.document.CreateDocumentWithFileCommand;
import com.example.factoryguard.application.dto.document.DocumentCreateResult;
import com.example.factoryguard.application.dto.document.DocumentDetailResult;
import com.example.factoryguard.application.dto.document.DocumentDeindexRequest;
import com.example.factoryguard.application.dto.document.DocumentIndexRequest;
import com.example.factoryguard.application.dto.document.DocumentIndexResponse;
import com.example.factoryguard.application.dto.document.DocumentIndexingTarget;
import com.example.factoryguard.application.dto.document.DocumentListPageResult;
import com.example.factoryguard.application.dto.document.DocumentSearchQuery;
import com.example.factoryguard.application.dto.document.DocumentSummaryResult;
import com.example.factoryguard.application.dto.document.DocumentVersionDetailResult;
import com.example.factoryguard.application.dto.document.UpdateDocumentMetadataCommand;
import com.example.factoryguard.application.port.in.document.DocumentCrudUseCase;
import com.example.factoryguard.application.port.out.document.DocumentCrudPort;
import com.example.factoryguard.application.port.out.document.DocumentIndexPersistencePort;
import com.example.factoryguard.application.port.out.document.DocumentIndexingAiPort;
import com.example.factoryguard.application.port.out.file.PersistUploadedFilePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.client.AiServerProperties;
import com.example.factoryguard.config.document.DocumentUploadProperties;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import com.example.factoryguard.domain.document.vo.IndexingStatus;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.file.vo.StorageType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentCrudService implements DocumentCrudUseCase {

    private static final Logger log = LoggerFactory.getLogger(DocumentCrudService.class);

    private final DocumentCrudPort documentCrudPort;
    private final DocumentIndexPersistencePort documentIndexPersistencePort;
    private final DocumentIndexingAiPort documentIndexingAiPort;
    private final PersistUploadedFilePort persistUploadedFilePort;
    private final SecurityUtils securityUtils;
    private final MinioStorageAdapter minioStorageAdapter;
    private final MinioProperties minioProperties;
    private final DocumentUploadProperties documentUploadProperties;
    private final AiServerProperties aiServerProperties;
    private final TransactionOperations transactionOperations;

    @Override
    @Transactional(readOnly = true)
    public DocumentListPageResult listDocuments(DocumentSearchQuery query) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        boolean isAdmin = isAdmin(principal);
        Long organizationId = isAdmin ? null : securityUtils.requireOrganizationId();
        return documentCrudPort.findDocuments(query, organizationId, isAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentSummaryResult getSummary() {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        boolean isAdmin = isAdmin(principal);
        Long organizationId = isAdmin ? null : securityUtils.requireOrganizationId();
        return documentCrudPort.summarize(organizationId, isAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDetailResult getDetail(Long documentId) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        boolean isAdmin = isAdmin(principal);
        Long organizationId = isAdmin ? null : securityUtils.requireOrganizationId();
        validateOrganizationAccess(documentId, organizationId, isAdmin);
        return documentCrudPort.findDetail(documentId, organizationId, isAdmin)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "문서를 찾을 수 없습니다."));
    }

    @Override
    public DocumentCreateResult createDocument(CreateDocumentWithFileCommand command) {
        requireCompanyAdminOrSiteAdmin();
        validateFile(command.getFile());
        List<String> normalizedTags = normalizeTags(command.getTags());
        StoredFile storedFile = uploadAndPersist(command.getFile(), command.getUserId());
        DocumentCreateResult created = transactionOperations.execute(status -> documentCrudPort.createDocument(
                    command.getOrganizationId(),
                    command.getUserId(),
                    command.getTitle(),
                    detectDocumentType(command.getFile().getOriginalFilename()),
                    command.getCategory(),
                    command.getEquipmentType(),
                    command.getDescription(),
                    normalizedTags,
                    storedFile.getFileId(),
                    String.valueOf(command.getUserId())
            ));
        DocumentIndexingTarget target = DocumentIndexingTarget.builder()
                .documentId(created.getDocumentId())
                .documentVersionId(created.getDocumentVersionId())
                .fileId(storedFile.getFileId())
                .fileKey(storedFile.getObjectKey())
                .fileName(storedFile.getFileName())
                .mimeType(storedFile.getMimeType())
                .checksum(storedFile.getChecksum())
                .documentType(com.example.factoryguard.domain.document.vo.DocumentType.valueOf(detectDocumentType(command.getFile().getOriginalFilename())))
                .organizationId(command.getOrganizationId())
                .title(command.getTitle())
                .category(command.getCategory())
                .equipmentType(command.getEquipmentType())
                .tags(normalizedTags)
                .build();
        return enqueueIndexing(created, target, null);
    }

    @Override
    @Transactional
    public DocumentDetailResult updateMetadata(UpdateDocumentMetadataCommand command) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        boolean isAdmin = isAdmin(principal);
        Long organizationId = isAdmin ? null : securityUtils.requireOrganizationId();
        validateOrganizationAccess(command.getDocumentId(), organizationId, isAdmin);
        List<String> normalizedTags = normalizeTags(command.getTags());
        return documentCrudPort.updateMetadata(
                command.getDocumentId(),
                organizationId,
                isAdmin,
                command.getTitle(),
                command.getCategory(),
                command.getEquipmentType(),
                command.getDescription(),
                normalizedTags
        );
    }

    @Override
    public DocumentCreateResult createVersion(CreateDocumentVersionCommand command) {
        requireCompanyAdminOrSiteAdmin();
        validateFile(command.getFile());
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        boolean isAdmin = isAdmin(principal);
        Long organizationId = isAdmin ? null : securityUtils.requireOrganizationId();
        validateOrganizationAccess(command.getDocumentId(), organizationId, isAdmin);
        StoredFile storedFile = uploadAndPersist(command.getFile(), command.getUserId());
        String fileHash = calculateSha256(command.getFile());
        DocumentVersionDetailResult version = transactionOperations.execute(status -> documentCrudPort.createVersion(
                    command.getDocumentId(),
                    organizationId,
                    isAdmin,
                    storedFile.getFileId(),
                    fileHash,
                    command.getChangeReason()
            ));
        DocumentCreateResult created = DocumentCreateResult.builder()
                .documentId(command.getDocumentId())
                .documentVersionId(version.getDocumentVersionId())
                .indexJobId(version.getIndexJobId())
                .indexingStatus(version.getIndexingStatus())
                .build();
        DocumentIndexingTarget target = documentIndexPersistencePort.findIndexingTarget(version.getDocumentVersionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "문서 인덱싱 대상을 찾을 수 없습니다."));
        return enqueueIndexing(created, target, null);
    }

    @Override
    public void softDelete(Long documentId) {
        requireCompanyAdminOrSiteAdmin();
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        boolean isAdmin = isAdmin(principal);
        Long organizationId = isAdmin ? null : securityUtils.requireOrganizationId();
        validateOrganizationAccess(documentId, organizationId, isAdmin);
        DocumentIndexingTarget target = documentIndexPersistencePort.findLatestIndexingTargetByDocumentId(documentId)
                .orElse(null);
        boolean deleted = Boolean.TRUE.equals(transactionOperations.execute(status ->
                documentCrudPort.softDelete(documentId, organizationId, isAdmin)));
        if (!deleted) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "문서를 찾을 수 없습니다.");
        }
        if (target != null) {
            try {
                documentIndexingAiPort.deindex(
                        target.getDocumentVersionId(),
                        DocumentDeindexRequest.builder()
                                .organizationId(target.getOrganizationId())
                                .documentId(target.getDocumentId())
                                .reason("DOCUMENT_DELETED")
                                .build(),
                        null
                );
            } catch (RuntimeException exception) {
                transactionOperations.executeWithoutResult(status ->
                        documentIndexPersistencePort.recordDeindexFailure(target.getDocumentVersionId(), exception.getMessage()));
                log.warn("FastAPI deindex failed after document delete, documentId={}, documentVersionId={}, organizationId={}",
                        target.getDocumentId(), target.getDocumentVersionId(), target.getOrganizationId());
            }
        }
    }

    private DocumentCreateResult enqueueIndexing(DocumentCreateResult created, DocumentIndexingTarget target, String requestId) {
        try {
            DocumentIndexResponse response = documentIndexingAiPort.enqueue(buildIndexRequest(created.getIndexJobId(), target), requestId);
            transactionOperations.executeWithoutResult(status -> documentIndexPersistencePort.markEnqueued(
                    created.getDocumentId(),
                    created.getDocumentVersionId(),
                    created.getIndexJobId(),
                    response.getAiJobId()
            ));
            return DocumentCreateResult.builder()
                    .documentId(created.getDocumentId())
                    .documentVersionId(created.getDocumentVersionId())
                    .indexJobId(created.getIndexJobId())
                    .aiJobId(response.getAiJobId())
                    .indexingStatus(IndexingStatus.PROCESSING)
                    .build();
        } catch (RuntimeException exception) {
            transactionOperations.executeWithoutResult(status -> documentIndexPersistencePort.markFailed(
                    created.getDocumentId(),
                    created.getDocumentVersionId(),
                    created.getIndexJobId(),
                    exception.getMessage()
            ));
            log.warn("FastAPI document indexing enqueue failed, documentId={}, documentVersionId={}, organizationId={}",
                    created.getDocumentId(), created.getDocumentVersionId(), target.getOrganizationId());
            return DocumentCreateResult.builder()
                    .documentId(created.getDocumentId())
                    .documentVersionId(created.getDocumentVersionId())
                    .indexJobId(created.getIndexJobId())
                    .indexingStatus(IndexingStatus.FAILED)
                    .build();
        }
    }

    private DocumentIndexRequest buildIndexRequest(Long indexJobId, DocumentIndexingTarget target) {
        return DocumentIndexRequest.builder()
                .indexJobId(indexJobId)
                .documentId(target.getDocumentId())
                .documentVersionId(target.getDocumentVersionId())
                .organizationId(target.getOrganizationId())
                .fileId(target.getFileId())
                .fileKey(target.getFileKey())
                .fileName(target.getFileName())
                .mimeType(target.getMimeType())
                .checksum(target.getChecksum())
                .title(target.getTitle())
                .documentType(target.getDocumentType() == null ? null : target.getDocumentType().name())
                .category(target.getCategory())
                .equipmentType(target.getEquipmentType())
                .tags(target.getTags())
                .chunkSize(aiServerProperties.getDocumentIndex().getDefaultChunkSize())
                .chunkOverlap(aiServerProperties.getDocumentIndex().getDefaultChunkOverlap())
                .embeddingModel(aiServerProperties.getDocumentIndex().getDefaultEmbeddingModel())
                .build();
    }

    private StoredFile uploadAndPersist(MultipartFile file, Long userId) {
        String bucket = minioProperties.getBucketDocuments();
        String objectKey = buildObjectKey(file.getOriginalFilename());
        String checksum = calculateSha256(file);
        try {
            minioStorageAdapter.upload(
                    bucket,
                    objectKey,
                    file.getInputStream(),
                    file.getSize(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    checksum
            );
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "문서 파일 업로드에 실패했습니다.");
        }

        StoredFile fileDomain = StoredFile.builder()
                .storageType(StorageType.MINIO)
                .bucketName(bucket)
                .objectKey(objectKey)
                .filePath(null)
                .fileName(file.getOriginalFilename())
                .fileExt(fileExt(file.getOriginalFilename()))
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .checksum(checksum)
                .createdAt(LocalDateTime.now())
                .createdBy(userId)
                .build();
        return persistUploadedFilePort.save(fileDomain);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "파일은 필수입니다.");
        }
        if (file.getSize() > documentUploadProperties.getMaxSizeBytes()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "파일 최대 용량을 초과했습니다.");
        }
        String ext = fileExt(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        List<String> allowedExtensions = documentUploadProperties.getAllowedExtensions().stream()
                .map(v -> v.toLowerCase(Locale.ROOT))
                .toList();
        if (!allowedExtensions.contains(ext)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "허용되지 않은 파일 형식입니다.");
        }
        String mimeType = file.getContentType();
        if (mimeType == null || documentUploadProperties.getAllowedMimeTypes().stream().noneMatch(mimeType::equalsIgnoreCase)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "허용되지 않은 MIME 타입입니다.");
        }
        validateMimeMatchesExtension(file.getOriginalFilename(), mimeType);
        validateMagicBytes(file, ext);
    }

    private void validateMagicBytes(MultipartFile file, String ext) {
        byte[] header;
        try (InputStream input = file.getInputStream()) {
            header = input.readNBytes(8);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "파일 헤더를 읽을 수 없습니다.");
        }
        if (header.length < 4) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "파일 내용이 손상되었거나 너무 짧습니다.");
        }
        switch (ext) {
            case "pdf" -> {
                if (!(header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46)) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "PDF 파일 시그니처가 올바르지 않습니다.");
                }
            }
            case "docx" -> {
                if (!(header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04)) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DOCX 파일 시그니처가 올바르지 않습니다.");
                }
            }
            case "md" -> {
                for (int i = 0; i < Math.min(header.length, 4); i++) {
                    byte b = header[i];
                    if (b == 0x00) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "MD 파일에 NUL 바이트가 포함되어 있습니다.");
                    }
                }
            }
            default -> { /* allowedExtensions가 이미 제한 — 추가 분기 불필요 */ }
        }
    }

    private void validateMimeMatchesExtension(String originalFilename, String mimeType) {
        String ext = fileExt(originalFilename).toLowerCase(Locale.ROOT);
        if (ext.isBlank()) {
            return;
        }
        String expected = switch (ext) {
            case "pdf" -> "application/pdf";
            case "md" -> {
                if ("text/plain".equalsIgnoreCase(mimeType)) {
                    yield "text/plain";
                }
                yield "text/markdown";
            }
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> null;
        };
        if (expected != null && !expected.equalsIgnoreCase(mimeType)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "파일 확장자와 MIME 타입이 일치하지 않습니다.");
        }
    }

    private String detectDocumentType(String fileName) {
        String ext = fileExt(fileName).toUpperCase(Locale.ROOT);
        if (ext.isBlank()) {
            return "TXT";
        }
        return ext;
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        Set<String> deduped = new LinkedHashSet<>();
        for (String rawTag : tags) {
            if (rawTag == null) {
                continue;
            }
            String trimmed = rawTag.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() > 50) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "태그는 50자를 초과할 수 없습니다.");
            }
            if (trimmed.contains("<") || trimmed.contains(">")) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "태그에 허용되지 않은 문자가 포함되어 있습니다.");
            }
            deduped.add(trimmed);
        }
        if (deduped.size() > 10) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "태그는 최대 10개까지 등록할 수 있습니다.");
        }
        return new ArrayList<>(deduped);
    }

    private String fileExt(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    private String calculateSha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "파일 해시 계산에 실패했습니다.");
        }
    }

    private boolean isAdmin(AuthenticatedPrincipal principal) {
        if (principal.role() == null) {
            return false;
        }
        String normalized = principal.role().trim().toUpperCase(Locale.ROOT);
        return "ROLE_SITE_ADMIN".equals(normalized) || "ADMIN".equals(normalized);
    }

    private void requireCompanyAdminOrSiteAdmin() {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        String role = principal.role() == null ? "" : principal.role().trim().toUpperCase(Locale.ROOT);
        if (!"ROLE_COMPANY_ADMIN".equals(role) && !"ROLE_SITE_ADMIN".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "문서 업로드/수정/삭제는 회사 관리자 이상만 가능합니다.");
        }
    }

    private void validateOrganizationAccess(Long documentId, Long organizationId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        Long targetOrganizationId = documentCrudPort.findDocumentOrganizationId(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "문서를 찾을 수 없습니다."));
        if (!targetOrganizationId.equals(organizationId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "접근 권한이 없는 문서입니다.");
        }
    }

    private String buildObjectKey(String originalFileName) {
        String ext = fileExt(originalFileName).toLowerCase(Locale.ROOT);
        String prefix = documentUploadProperties.getObjectPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "documents";
        }
        if (ext.isBlank()) {
            return prefix + "/" + UUID.randomUUID();
        }
        return prefix + "/" + UUID.randomUUID() + "." + ext;
    }
}
