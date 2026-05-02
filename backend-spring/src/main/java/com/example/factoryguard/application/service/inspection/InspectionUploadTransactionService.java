package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.port.out.file.PersistUploadedFilePort;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionEventLogPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionInputPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionRunPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.inspection.model.InspectionEventLog;
import com.example.factoryguard.domain.inspection.model.InspectionEventType;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InspectionUploadTransactionService {

    private final SaveInspectionRunPort saveInspectionRunPort;
    private final LoadInspectionRunPort loadInspectionRunPort;
    private final PersistUploadedFilePort persistUploadedFilePort;
    private final SaveInspectionInputPort saveInspectionInputPort;
    private final SaveInspectionEventLogPort saveInspectionEventLogPort;

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
        saveInspectionEventLogPort.save(event(inspectionId, InspectionEventType.PROCESS_STARTED, "processing queued"));
        return storedFile;
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
