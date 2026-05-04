package com.example.factoryguard.application.service.document;

import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.application.dto.document.CreateDocumentVersionCommand;
import com.example.factoryguard.application.dto.document.CreateDocumentWithFileCommand;
import com.example.factoryguard.application.dto.document.DocumentCreateResult;
import com.example.factoryguard.application.dto.document.DocumentVersionDetailResult;
import com.example.factoryguard.application.port.out.document.DocumentCrudPort;
import com.example.factoryguard.application.port.out.file.PersistUploadedFilePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.document.DocumentUploadProperties;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import com.example.factoryguard.domain.document.vo.IndexingStatus;
import com.example.factoryguard.domain.file.model.StoredFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCrudServiceTest {

    @Mock private DocumentCrudPort documentCrudPort;
    @Mock private PersistUploadedFilePort persistUploadedFilePort;
    @Mock private SecurityUtils securityUtils;
    @Mock private MinioStorageAdapter minioStorageAdapter;
    @Mock private MinioProperties minioProperties;

    private DocumentUploadProperties uploadProperties;
    private DocumentCrudService service;

    @BeforeEach
    void setUp() {
        uploadProperties = new DocumentUploadProperties();
        service = new DocumentCrudService(
                documentCrudPort,
                persistUploadedFilePort,
                securityUtils,
                minioStorageAdapter,
                minioProperties,
                uploadProperties
        );
        lenient().when(minioProperties.getBucketDocuments()).thenReturn("documents");
        lenient().when(persistUploadedFilePort.save(any())).thenAnswer(invocation -> {
            StoredFile in = invocation.getArgument(0);
            return StoredFile.builder()
                    .fileId(900L)
                    .storageType(in.getStorageType())
                    .bucketName(in.getBucketName())
                    .objectKey(in.getObjectKey())
                    .fileName(in.getFileName())
                    .fileExt(in.getFileExt())
                    .mimeType(in.getMimeType())
                    .fileSize(in.getFileSize())
                    .checksum(in.getChecksum())
                    .createdAt(in.getCreatedAt())
                    .createdBy(in.getCreatedBy())
                    .build();
        });
    }

    @Test
    @DisplayName("No.25 허용 형식 PDF 업로드 - 정상 생성")
    void createsDocumentWithAllowedPdf() {
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "manual.pdf", "application/pdf",
                new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x37});
        when(documentCrudPort.createDocument(anyLong(), anyLong(), anyString(), anyString(),
                any(), any(), any(), any(), anyLong(), anyString()))
                .thenReturn(DocumentCreateResult.builder()
                        .documentId(10L).documentVersionId(11L).indexingStatus(IndexingStatus.PENDING).build());

        CreateDocumentWithFileCommand command = CreateDocumentWithFileCommand.builder()
                .userId(1L).organizationId(100L).file(pdf).title("매뉴얼")
                .category("INSPECTION").equipmentType("PRESS").description("desc")
                .tags(List.of("press", "v1")).build();

        DocumentCreateResult result = service.createDocument(command);

        assertThat(result.getDocumentId()).isEqualTo(10L);
        assertThat(result.getIndexingStatus()).isEqualTo(IndexingStatus.PENDING);
        verify(documentCrudPort).createDocument(eq(100L), eq(1L), eq("매뉴얼"),
                eq("PDF"), any(), any(), any(), any(), eq(900L), eq("1"));
    }

    @Test
    @DisplayName("No.25 허용되지 않은 확장자(exe) - VALIDATION_FAILED")
    void rejectsDisallowedExtension() {
        MockMultipartFile exe = new MockMultipartFile(
                "file", "malware.exe", "application/octet-stream", new byte[]{1, 2});
        CreateDocumentWithFileCommand command = CreateDocumentWithFileCommand.builder()
                .userId(1L).organizationId(100L).file(exe).title("악성").build();

        assertThatThrownBy(() -> service.createDocument(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);
    }

    @Test
    @DisplayName("No.25 빈 파일 차단 - VALIDATION_FAILED")
    void rejectsEmptyFile() {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "manual.pdf", "application/pdf", new byte[0]);
        CreateDocumentWithFileCommand command = CreateDocumentWithFileCommand.builder()
                .userId(1L).organizationId(100L).file(empty).title("빈 파일").build();

        assertThatThrownBy(() -> service.createDocument(command))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("No.25 magic byte 위장(.exe를 PDF MIME으로 위장) - VALIDATION_FAILED")
    void rejectsMimeSpoofedExeAsPdf() {
        MockMultipartFile fakePdf = new MockMultipartFile(
                "file", "evil.pdf", "application/pdf",
                new byte[]{0x4D, 0x5A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        CreateDocumentWithFileCommand command = CreateDocumentWithFileCommand.builder()
                .userId(1L).organizationId(100L).file(fakePdf).title("위장 PDF").build();

        assertThatThrownBy(() -> service.createDocument(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);
    }

    @Test
    @DisplayName("No.25 magic byte 위장(잘못된 DOCX 헤더) - VALIDATION_FAILED")
    void rejectsInvalidDocxMagicBytes() {
        MockMultipartFile fakeDocx = new MockMultipartFile(
                "file", "fake.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07});
        CreateDocumentWithFileCommand command = CreateDocumentWithFileCommand.builder()
                .userId(1L).organizationId(100L).file(fakeDocx).title("위장 DOCX").build();

        assertThatThrownBy(() -> service.createDocument(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);
    }

    @Test
    @DisplayName("No.26 새 버전 추가 - createVersion Port 호출 및 documentId 유지")
    void addsNewVersionToExistingDocument() {
        long documentId = 10L;
        long userId = 1L;
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(userId, "ROLE_COMPANY_WORKER", 100L, "sess");
        when(securityUtils.getCurrentPrincipal()).thenReturn(principal);
        when(securityUtils.requireOrganizationId()).thenReturn(100L);
        when(documentCrudPort.findDocumentOrganizationId(documentId)).thenReturn(Optional.of(100L));

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "manual-v2.pdf", "application/pdf",
                new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x37});

        when(documentCrudPort.createVersion(eq(documentId), eq(100L), anyBoolean(), anyLong(), anyString(), any()))
                .thenReturn(DocumentVersionDetailResult.builder()
                        .documentVersionId(22L).versionNo(2).indexingStatus(IndexingStatus.PENDING).build());

        DocumentCreateResult result = service.createVersion(CreateDocumentVersionCommand.builder()
                .userId(userId).organizationId(100L).documentId(documentId).file(pdf)
                .changeReason("업데이트").build());

        assertThat(result.getDocumentId()).isEqualTo(documentId);
        assertThat(result.getDocumentVersionId()).isEqualTo(22L);
        verify(documentCrudPort).createVersion(eq(documentId), eq(100L), anyBoolean(), eq(900L), anyString(), eq("업데이트"));
    }
}
