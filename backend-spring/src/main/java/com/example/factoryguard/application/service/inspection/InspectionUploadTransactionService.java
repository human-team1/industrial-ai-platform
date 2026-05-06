package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.port.out.file.PersistUploadedFilePort;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionEventLogPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionInputPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionRunPort;
import com.example.factoryguard.application.port.out.operation.SaveAsyncJobPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.inspection.model.InspectionEventLog;
import com.example.factoryguard.domain.inspection.model.InspectionEventType;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import com.example.factoryguard.domain.operation.model.AsyncJob;
import com.example.factoryguard.domain.operation.vo.AsyncJobStatus;
import com.example.factoryguard.domain.operation.vo.AsyncJobType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InspectionUploadTransactionService {

    private final SaveInspectionRunPort saveInspectionRunPort;
    private final LoadInspectionRunPort loadInspectionRunPort;
    private final PersistUploadedFilePort persistUploadedFilePort;
    private final SaveInspectionInputPort saveInspectionInputPort;
    private final SaveInspectionEventLogPort saveInspectionEventLogPort;
    private final SaveAsyncJobPort saveAsyncJobPort;

    @Transactional
    public InspectionRun createPendingRun(InspectionRun run) {
        return saveInspectionRunPort.save(run);
    }

    @Transactional
    public StoredFile persistFileInputAndMarkProcessing(
            Long inspectionId,
            StoredFile file,
            InspectionInput input,
            String originalFileName
    ) {
        StoredFile storedFile = persistUploadedFilePort.save(file);
        saveInspectionInputPort.save(InspectionInput.builder()
                .inspectionId(input.getInspectionId())
                .fileId(storedFile.getFileId())
                .cameraId(input.getCameraId())
                .streamUrl(input.getStreamUrl())
                .sourceType(input.getSourceType())
                .sourceName(input.getSourceName())
                .mimeType(input.getMimeType())
                .durationSec(input.getDurationSec())
                .frameCount(input.getFrameCount())
                .build());
        saveInspectionEventLogPort.save(event(inspectionId, InspectionEventType.UPLOAD_RECEIVED, originalFileName));
        saveInspectionEventLogPort.save(event(inspectionId, InspectionEventType.INPUT_SAVED, "input persisted"));

        InspectionRun existing = loadInspectionRunPort.findRunById(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_NOT_FOUND));
        saveInspectionRunPort.save(existing.toBuilder()
                .runStatus(RunStatus.PROCESSING)
                .build());
        saveAsyncJobPort.save(AsyncJob.builder()
                .jobType(AsyncJobType.AI_IMAGE_INFERENCE)
                .jobStatus(AsyncJobStatus.PENDING)
                .targetType("INSPECTION")
                .targetId(inspectionId)
                .createdAt(LocalDateTime.now())
                .build());
        saveInspectionEventLogPort.save(event(inspectionId, InspectionEventType.PROCESS_STARTED, "processing queued"));
        return storedFile;
    }

    @Transactional
    public void persistExistingInputAndMarkProcessing(
            Long inspectionId,
            InspectionInput input,
            String sourceName
    ) {
        saveInspectionInputPort.save(InspectionInput.builder()
                .inspectionId(input.getInspectionId())
                .fileId(input.getFileId())
                .cameraId(input.getCameraId())
                .streamUrl(input.getStreamUrl())
                .sourceType(input.getSourceType())
                .sourceName(input.getSourceName())
                .mimeType(input.getMimeType())
                .durationSec(input.getDurationSec())
                .frameCount(input.getFrameCount())
                .roiMode(input.getRoiMode())
                .roiCoordinateType(input.getRoiCoordinateType())
                .roiX(input.getRoiX())
                .roiY(input.getRoiY())
                .roiWidth(input.getRoiWidth())
                .roiHeight(input.getRoiHeight())
                .qualityGateEnabled(input.getQualityGateEnabled())
                .build());
        saveInspectionEventLogPort.save(event(inspectionId, InspectionEventType.UPLOAD_RECEIVED, sourceName));
        saveInspectionEventLogPort.save(event(inspectionId, InspectionEventType.INPUT_SAVED, "input persisted"));

        InspectionRun existing = loadInspectionRunPort.findRunById(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_NOT_FOUND));
        saveInspectionRunPort.save(existing.toBuilder()
                .runStatus(RunStatus.PROCESSING)
                .build());
        saveAsyncJobPort.save(AsyncJob.builder()
                .jobType(AsyncJobType.AI_IMAGE_INFERENCE)
                .jobStatus(AsyncJobStatus.PENDING)
                .targetType("INSPECTION")
                .targetId(inspectionId)
                .createdAt(LocalDateTime.now())
                .build());
        saveInspectionEventLogPort.save(event(inspectionId, InspectionEventType.PROCESS_STARTED, "processing queued"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailedRequiresNew(Long inspectionId, String errorCode, String message) {
        InspectionRun existing = loadInspectionRunPort.findRunById(inspectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_NOT_FOUND));
        saveInspectionRunPort.save(existing.toBuilder()
                .runStatus(RunStatus.FAILED)
                .errorCode(errorCode)
                .build());
        saveInspectionEventLogPort.save(event(inspectionId, InspectionEventType.FAILED, message));
    }

    private InspectionEventLog event(Long inspectionId, InspectionEventType type, String message) {
        return InspectionEventLog.builder()
                .inspectionId(inspectionId)
                .eventType(type)
                .message(message)
                .build();
    }
}
