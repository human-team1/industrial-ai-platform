package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.domain.inspection.model.InspectionEventType;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InspectionStaleRunSweeper {

    private final LoadInspectionRunPort loadInspectionRunPort;
    private final InspectionRunRecorder runRecorder;
    private final InspectionEventLogger eventLogger;

    @Scheduled(fixedDelay = 60_000L)
    public void sweep() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<InspectionRun> stale = loadInspectionRunPort
                .findRunsByStatusAndStartedAtBefore(RunStatus.PENDING, threshold);
        if (stale.isEmpty()) {
            return;
        }
        log.warn("Sweeping {} stale PENDING inspection runs", stale.size());
        for (InspectionRun run : stale) {
            try {
                runRecorder.markFailed(run.getInspectionId(), "STALE_PENDING_TIMEOUT");
                eventLogger.logFailure(run.getInspectionId(),
                        InspectionEventType.FAILED, "stale pending swept");
            } catch (Exception e) {
                log.error("Failed to sweep stale run {}", run.getInspectionId(), e);
            }
        }
    }
}
