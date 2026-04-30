package com.example.factoryguard.inspection;

import com.example.factoryguard.adapter.out.persistence.inspection.InspectionEventLogJpaRepository;
import com.example.factoryguard.adapter.out.persistence.inspection.InspectionInputJpaRepository;
import com.example.factoryguard.adapter.out.persistence.inspection.InspectionResultJpaEntity;
import com.example.factoryguard.adapter.out.persistence.inspection.InspectionResultJpaRepository;
import com.example.factoryguard.adapter.out.persistence.inspection.InspectionRunJpaEntity;
import com.example.factoryguard.adapter.out.persistence.inspection.InspectionRunJpaRepository;
import com.example.factoryguard.adapter.out.persistence.result.AnomalyRegionJpaRepository;
import com.example.factoryguard.adapter.out.persistence.result.ImageJpaRepository;
import com.example.factoryguard.adapter.out.persistence.result.ResultArtifactJpaRepository;
import com.example.factoryguard.adapter.out.persistence.review.ReviewQueueJpaEntity;
import com.example.factoryguard.adapter.out.persistence.review.ReviewQueueJpaRepository;
import com.example.factoryguard.adapter.out.persistence.user.UserJpaEntity;
import com.example.factoryguard.adapter.out.persistence.user.UserJpaRepository;
import com.example.factoryguard.adapter.out.persistence.user.UserThresholdHistoryJpaRepository;
import com.example.factoryguard.adapter.out.persistence.user.UserThresholdJpaRepository;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.adapter.out.storage.minio.StoredObjectMetadata;
import com.example.factoryguard.application.dto.inspection.AiInspectionResponse;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.inspection.CallAiInspectionPort;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import com.example.factoryguard.domain.inspection.model.RunType;
import com.example.factoryguard.domain.review.vo.ReviewQueueStatus;
import com.example.factoryguard.domain.review.vo.ReviewQueuedReason;
import com.example.factoryguard.support.InspectionFixture;
import com.example.factoryguard.support.MariaDbContainerInitializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = MariaDbContainerInitializer.class)
class InspectionUploadIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired InspectionFixture fixture;
    @Autowired JdbcTemplate jdbcTemplate;

    @Autowired InspectionRunJpaRepository inspectionRunRepository;
    @Autowired InspectionResultJpaRepository inspectionResultRepository;
    @Autowired InspectionInputJpaRepository inspectionInputRepository;
    @Autowired InspectionEventLogJpaRepository inspectionEventLogRepository;
    @Autowired ReviewQueueJpaRepository reviewQueueRepository;
    @Autowired ResultArtifactJpaRepository resultArtifactRepository;
    @Autowired ImageJpaRepository imageRepository;
    @Autowired AnomalyRegionJpaRepository anomalyRegionRepository;
    @Autowired UserThresholdHistoryJpaRepository userThresholdHistoryRepository;
    @Autowired UserThresholdJpaRepository userThresholdRepository;
    @Autowired UserJpaRepository userJpaRepository;

    @MockBean CallAiInspectionPort callAiInspectionPort;
    @MockBean MinioStorageAdapter minioStorageAdapter;
    @MockBean SecurityUtils securityUtils;
    @MockBean TokenStorePort tokenStorePort;

    private Long orgId;
    private UserJpaEntity user;
    private Long targetId;
    private static final String SESSION_ID = "test-session-id";

    @BeforeEach
    void setUp() {
        orgId = fixture.createOrganization("test-org-" + UUID.randomUUID());
        user = fixture.createUser(orgId);
        targetId = fixture.createAnalysisTarget(orgId);
        fixture.createUserThreshold(user.getUserId(), 0.75, 0.55);

        when(securityUtils.getCurrentPrincipal())
                .thenReturn(new AuthenticatedPrincipal(user.getUserId(), "USER", orgId, SESSION_ID));
        when(tokenStorePort.getSessionId(anyLong())).thenReturn(Optional.of(SESSION_ID));

        when(minioStorageAdapter.upload(any(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new StoredObjectMetadata("inspection-artifacts", "key", "test.jpg", "image/jpeg", 4L, ""));
        when(minioStorageAdapter.createPresignedGetUrl(any(), any(), any()))
                .thenReturn("http://dummy.local/test.jpg");
    }

    @AfterEach
    void cleanup() {
        reviewQueueRepository.deleteAllInBatch();
        anomalyRegionRepository.deleteAllInBatch();
        imageRepository.deleteAllInBatch();
        resultArtifactRepository.deleteAllInBatch();
        inspectionResultRepository.deleteAllInBatch();
        inspectionEventLogRepository.deleteAllInBatch();
        inspectionInputRepository.deleteAllInBatch();
        inspectionRunRepository.deleteAllInBatch();
        userThresholdHistoryRepository.deleteAllInBatch();
        userThresholdRepository.deleteAllInBatch();
        jdbcTemplate.update("DELETE FROM analysis_target");
        userJpaRepository.deleteAllInBatch();
        jdbcTemplate.update("DELETE FROM ORGANIZATION");
    }

    private MockMultipartFile validJpegFile() {
        return new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}
        );
    }

    private void stubAiResponse(double score, double confidence) {
        AiInspectionResponse response = mock(AiInspectionResponse.class);
        when(response.getScore()).thenReturn(score);
        when(response.getConfidence()).thenReturn(confidence);
        when(response.getModelVersionId()).thenReturn(null);
        try {
            when(callAiInspectionPort.call(any())).thenReturn(response);
        } catch (TimeoutException e) {
            throw new IllegalStateException(e);
        }
    }

    private Long extractInspectionId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("inspectionId").asLong();
    }

    @Test
    @DisplayName("1. 정상 업로드 → InspectionResult 생성")
    void upload_normal_persistsRunAndResult() throws Exception {
        stubAiResponse(0.3, 0.9);

        mockMvc.perform(multipart("/api/v1/inspections/upload")
                        .file(validJpegFile())
                        .param("targetId", targetId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.result.resultStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.data.result.decisionCode").value("NORMAL"));

        assertThat(inspectionRunRepository.count()).isEqualTo(1);
        assertThat(inspectionResultRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("2. low confidence → ReviewQueue 생성")
    void upload_lowConfidence_enqueuesReviewQueue() throws Exception {
        // confidence 0.3 < lowConfidenceThreshold 0.55 → RECHECK / LOW_CONFIDENCE
        stubAiResponse(0.5, 0.3);

        mockMvc.perform(multipart("/api/v1/inspections/upload")
                        .file(validJpegFile())
                        .param("targetId", targetId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.decisionCode").value("RECHECK"))
                .andExpect(jsonPath("$.data.result.resultStatus").value("REVIEW_REQUIRED"))
                .andExpect(jsonPath("$.data.result.reviewQueued").value(true));

        assertThat(reviewQueueRepository.count()).isEqualTo(1);
        ReviewQueueJpaEntity rq = reviewQueueRepository.findAll().get(0);
        assertThat(rq.getQueueStatus()).isEqualTo(ReviewQueueStatus.WAITING);
        assertThat(rq.getQueuedReason()).isEqualTo(ReviewQueuedReason.LOW_CONFIDENCE);
    }

    @Test
    @DisplayName("3. invalid file → 422 ProblemDetails")
    void upload_invalidFile_returns422() throws Exception {
        MockMultipartFile bad = new MockMultipartFile(
                "file", "malware.exe", "application/octet-stream", new byte[]{0x4D, 0x5A});

        mockMvc.perform(multipart("/api/v1/inspections/upload")
                        .file(bad)
                        .param("targetId", targetId.toString()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.startsWith("FILE-")));

        assertThat(inspectionRunRepository.count()).isZero();
        assertThat(inspectionResultRepository.count()).isZero();
    }

    @Test
    @DisplayName("4. AI timeout → RUN FAILED + Result FAILED + 504")
    void upload_aiTimeout_returns504AndFailedResult() throws Exception {
        when(callAiInspectionPort.call(any())).thenThrow(new TimeoutException("simulated"));

        mockMvc.perform(multipart("/api/v1/inspections/upload")
                        .file(validJpegFile())
                        .param("targetId", targetId.toString()))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.code").value("AI-504"));

        assertThat(inspectionRunRepository.count()).isEqualTo(1);
        InspectionRunJpaEntity run = inspectionRunRepository.findAll().get(0);
        assertThat(run.getRunStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(run.getErrorCode()).isEqualTo("AI_TIMEOUT");

        assertThat(inspectionResultRepository.count()).isEqualTo(1);
        InspectionResultJpaEntity result = inspectionResultRepository.findAll().get(0);
        assertThat(result.getResultStatus()).isEqualTo("FAILED");
        assertThat(result.getFailureReason()).isNotBlank();
    }

    @Test
    @DisplayName("5. duplicate Idempotency-Key + 같은 payload → 기존 결과 replay")
    void upload_duplicateIdempotencyKeyAndPayload_replays() throws Exception {
        stubAiResponse(0.3, 0.9);
        String key = "idem-" + UUID.randomUUID();

        MvcResult first = mockMvc.perform(multipart("/api/v1/inspections/upload")
                        .file(validJpegFile())
                        .param("targetId", targetId.toString())
                        .header("Idempotency-Key", key))
                .andExpect(status().isOk())
                .andExpect(header().string("Idempotent-Replay", "false"))
                .andReturn();
        Long firstId = extractInspectionId(first);

        MvcResult second = mockMvc.perform(multipart("/api/v1/inspections/upload")
                        .file(validJpegFile())
                        .param("targetId", targetId.toString())
                        .header("Idempotency-Key", key))
                .andExpect(status().isOk())
                .andExpect(header().string("Idempotent-Replay", "true"))
                .andReturn();
        Long secondId = extractInspectionId(second);

        assertThat(secondId).isEqualTo(firstId);
        assertThat(inspectionRunRepository.count()).isEqualTo(1);
        assertThat(inspectionResultRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("6. COMPLETED 상태 STOP → 409")
    void stop_completedRun_returns409() throws Exception {
        InspectionRunJpaEntity run = inspectionRunRepository.save(InspectionRunJpaEntity.builder()
                .organizationId(orgId)
                .userId(user.getUserId())
                .targetId(targetId)
                .runType(RunType.UPLOAD)
                .inputType("FILE")
                .sourceType("UPLOAD")
                .sourceId("manual")
                .runStatus(RunStatus.COMPLETED)
                .appliedThreshold(0.75)
                .idempotencyKey("manual-" + UUID.randomUUID())
                .startedAt(LocalDateTime.now())
                .build());

        mockMvc.perform(patch("/api/v1/inspections/{id}/stop", run.getInspectionId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSPECTION-409"));

        assertThat(inspectionRunRepository.findById(run.getInspectionId()).orElseThrow().getRunStatus())
                .isEqualTo(RunStatus.COMPLETED);
    }

    @Test
    @DisplayName("7. 다른 조직 검사 GET → 403")
    void detail_otherOrganizationRun_returnsForbidden() throws Exception {
        Long otherOrgId = fixture.createOrganization("other-org-" + UUID.randomUUID());
        UserJpaEntity otherUser = fixture.createUser(otherOrgId);
        Long otherTargetId = fixture.createAnalysisTarget(otherOrgId);

        InspectionRunJpaEntity otherRun = inspectionRunRepository.save(InspectionRunJpaEntity.builder()
                .organizationId(otherOrgId)
                .userId(otherUser.getUserId())
                .targetId(otherTargetId)
                .runType(RunType.UPLOAD)
                .inputType("FILE")
                .sourceType("UPLOAD")
                .sourceId("manual")
                .runStatus(RunStatus.COMPLETED)
                .appliedThreshold(0.75)
                .idempotencyKey("other-" + UUID.randomUUID())
                .startedAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/v1/inspections/{id}", otherRun.getInspectionId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMMON-403"));
    }

    @Test
    @DisplayName("8. 같은 Idempotency-Key + 다른 payload → 409 IDEMPOTENCY_CONFLICT")
    void upload_sameKeyDifferentPayload_returns409Conflict() throws Exception {
        stubAiResponse(0.3, 0.9);
        String key = "idem-conflict-" + UUID.randomUUID();
        Long otherTargetId = fixture.createAnalysisTarget(orgId);

        mockMvc.perform(multipart("/api/v1/inspections/upload")
                        .file(validJpegFile())
                        .param("targetId", targetId.toString())
                        .header("Idempotency-Key", key))
                .andExpect(status().isOk());

        mockMvc.perform(multipart("/api/v1/inspections/upload")
                        .file(validJpegFile())
                        .param("targetId", otherTargetId.toString())  // payload 다름
                        .header("Idempotency-Key", key))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSPECTION-409B"));

        assertThat(inspectionRunRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("9. invalid X-Request-Id → 새 UUID echo + 정상 처리")
    void request_invalidXRequestIdHeader_replaces() throws Exception {
        stubAiResponse(0.3, 0.9);
        String invalid = "<script>alert(1)</script>";  // 허용외 문자

        mockMvc.perform(multipart("/api/v1/inspections/upload")
                        .file(validJpegFile())
                        .param("targetId", targetId.toString())
                        .header("X-Request-Id", invalid))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id",
                        org.hamcrest.Matchers.matchesRegex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")));
    }
}
