package com.example.factoryguard.application.service.operation;

import com.example.factoryguard.application.dto.operation.RecordAdminActionLogCommand;
import com.example.factoryguard.application.dto.operation.RecordAuditLogCommand;
import com.example.factoryguard.application.dto.operation.RecordOperationLogCommand;
import com.example.factoryguard.application.port.in.operation.RecordAdminActionLogUseCase;
import com.example.factoryguard.application.port.in.operation.RecordAuditLogUseCase;
import com.example.factoryguard.application.port.in.operation.RecordOperationLogUseCase;
import com.example.factoryguard.application.port.out.operation.OperationAdminPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogRecorderService implements
        RecordOperationLogUseCase,
        RecordAdminActionLogUseCase,
        RecordAuditLogUseCase {

    private final OperationAdminPort operationAdminPort;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordOperationLog(RecordOperationLogCommand command) {
        try {
            operationAdminPort.saveOperationLog(
                    command.getEventType(),
                    command.getEventStatus(),
                    command.getLogLevel(),
                    command.getSourceComponent(),
                    MDC.get("requestId"),
                    command.getActorUserId(),
                    command.getDetailMessage(),
                    command.getRelatedPath()
            );
        } catch (Exception exception) {
            log.warn("Failed to record operation log, requestId={}, eventType={}", MDC.get("requestId"), command.getEventType(), exception);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAdminActionLog(RecordAdminActionLogCommand command) {
        try {
            operationAdminPort.saveAdminActionLog(
                    command.getActorUserId(),
                    command.getActionType(),
                    command.getTargetType(),
                    command.getTargetId(),
                    command.getReason()
            );
        } catch (Exception exception) {
            log.warn("Failed to record admin action log, requestId={}, actionType={}", MDC.get("requestId"), command.getActionType(), exception);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAuditLog(RecordAuditLogCommand command) {
        try {
            operationAdminPort.saveAuditLog(
                    command.getActorUserId(),
                    command.getActionType(),
                    command.getTargetType(),
                    command.getTargetId(),
                    command.getBeforeJson(),
                    command.getAfterJson()
            );
        } catch (Exception exception) {
            log.warn("Failed to record audit log, requestId={}, actionType={}", MDC.get("requestId"), command.getActionType(), exception);
        }
    }
}
