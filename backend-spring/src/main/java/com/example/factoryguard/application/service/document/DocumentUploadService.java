package com.example.factoryguard.application.service.document;

import com.example.factoryguard.application.dto.document.DocumentIndexRequest;
import com.example.factoryguard.application.dto.document.DocumentIndexResponse;
import com.example.factoryguard.application.dto.document.DocumentUploadCommand;
import com.example.factoryguard.application.dto.document.DocumentUploadResult;
import com.example.factoryguard.application.port.in.document.UploadDocumentUseCase;
import com.example.factoryguard.application.port.out.document.CallDocumentIndexingPort;
import com.example.factoryguard.application.port.out.document.DocumentIndexPersistencePort;
import com.example.factoryguard.application.port.out.document.SaveUploadedDocumentPort;
import com.example.factoryguard.application.port.out.file.StoreDocumentFilePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.document.vo.DocumentIndexJobStatus;
import com.example.factoryguard.domain.document.vo.DocumentType;
import com.example.factoryguard.domain.document.vo.IndexingStatus;
import com.example.factoryguard.domain.file.model.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DocumentUploadService implements UploadDocumentUseCase {

    private static final long MAX_UPLOAD_BYTES = 50L * 1024L * 1024L;
    private static final Set<DocumentType> SUPPORTED_TYPES = Set.of(
            DocumentType.PDF,
            DocumentType.DOCX,
            DocumentType.MD,
            DocumentType.TXT
    );

    private final StoreDocumentFilePort storeDocumentFilePort;
    private final SaveUploadedDocumentPort saveUploadedDocumentPort;
    private final DocumentIndexPersistencePort documentIndexPersistencePort;
    private final CallDocumentIndexingPort callDocumentIndexingPort;

    @Override
    public DocumentUploadResult execute(DocumentUploadCommand command) {
        validate(command);

        String originalChecksum = sha256(command.getContent());
        StoredFile originalFile = storeDocumentFilePort.store(
                command.getContent(),
                command.getOriginalFileName(),
                command.getMimeType(),
                originalChecksum,
                command.getUserId()
        );

        DocumentUploadResult created = saveUploadedDocumentPort.save(
                command.getOrganizationId(),
                command.getUserId(),
                command.getTitle(),
                command.getDocumentType(),
                originalFile.getFileId(),
                originalChecksum,
                IndexingStatus.PROCESSING
        );
        Long jobId = documentIndexPersistencePort.createIndexJob(
                created.getDocumentVersionId(),
                DocumentIndexJobStatus.PROCESSING,
                LocalDateTime.now()
        );

        try {
            DocumentIndexResponse indexResponse = callDocumentIndexingPort.index(
                    DocumentIndexRequest.builder()
                            .indexJobId(jobId)
                            .documentId(created.getDocumentId())
                            .documentVersionId(created.getDocumentVersionId())
                            .fileId(originalFile.getFileId())
                            .fileKey(originalFile.getObjectKey())
                            .fileName(originalFile.getFileName())
                            .mimeType(originalFile.getMimeType())
                            .checksum(originalFile.getChecksum())
                            .documentType(command.getDocumentType().name())
                            .organizationId(command.getOrganizationId())
                            .title(command.getTitle())
                            .category(command.getCategory())
                            .equipmentType(command.getEquipmentType())
                            .tags(command.getTags())
                            .build(),
                    command.getRequestId()
            );
            documentIndexPersistencePort.markEnqueued(
                    created.getDocumentId(),
                    created.getDocumentVersionId(),
                    jobId,
                    indexResponse.getAiJobId()
            );
            return DocumentUploadResult.builder()
                    .documentId(created.getDocumentId())
                    .documentVersionId(created.getDocumentVersionId())
                    .fileId(originalFile.getFileId())
                    .indexJobId(jobId)
                    .indexingStatus(IndexingStatus.PROCESSING.name())
                    .build();
        } catch (BusinessException exception) {
            documentIndexPersistencePort.markFailed(
                    created.getDocumentId(),
                    created.getDocumentVersionId(),
                    jobId,
                    exception.getMessage()
            );
            throw exception;
        } catch (RuntimeException exception) {
            documentIndexPersistencePort.markFailed(
                    created.getDocumentId(),
                    created.getDocumentVersionId(),
                    jobId,
                    "문서 인덱싱 중 오류가 발생했습니다."
            );
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR, "문서 인덱싱 중 오류가 발생했습니다.");
        }
    }

    private void validate(DocumentUploadCommand command) {
        if (command.getUserId() == null || command.getOrganizationId() == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "문서 업로드 권한을 확인할 수 없습니다.");
        }
        if (command.getTitle() == null || command.getTitle().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "문서 제목은 필수입니다.");
        }
        if (command.getContent() == null || command.getContent().length == 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "업로드 파일은 필수입니다.");
        }
        if (command.getContent().length > MAX_UPLOAD_BYTES) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "문서 파일은 최대 50MB까지 업로드할 수 있습니다.");
        }
        if (command.getDocumentType() == null || !SUPPORTED_TYPES.contains(command.getDocumentType())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "지원하지 않는 문서 유형입니다.");
        }
        if (!matchesExtension(command.getOriginalFileName(), command.getDocumentType())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "문서 유형과 파일 확장자가 일치하지 않습니다.");
        }
    }

    private boolean matchesExtension(String fileName, DocumentType documentType) {
        String normalizedFileName = fileName == null ? "" : fileName.trim().toLowerCase(Locale.ROOT);
        return normalizedFileName.endsWith("." + documentType.name().toLowerCase(Locale.ROOT));
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "파일 해시 계산에 실패했습니다.");
        }
    }
}
