package com.example.factoryguard.application.service.notification;

import com.example.factoryguard.application.dto.notification.CreateNotificationCommand;
import com.example.factoryguard.application.dto.notification.ListNotificationsQuery;
import com.example.factoryguard.application.dto.notification.MarkAllReadResult;
import com.example.factoryguard.application.dto.notification.MarkNotificationReadCommand;
import com.example.factoryguard.application.dto.notification.NotificationDetail;
import com.example.factoryguard.application.dto.notification.NotificationPageResult;
import com.example.factoryguard.application.port.in.notification.CreateNotificationUseCase;
import com.example.factoryguard.application.port.in.notification.GetNotificationDetailUseCase;
import com.example.factoryguard.application.port.in.notification.GetNotificationsUseCase;
import com.example.factoryguard.application.port.in.notification.MarkAllNotificationsReadUseCase;
import com.example.factoryguard.application.port.in.notification.MarkNotificationReadUseCase;
import com.example.factoryguard.application.port.out.notification.LoadNotificationPort;
import com.example.factoryguard.application.port.out.notification.SaveNotificationPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.notification.exception.NotificationNotFoundException;
import com.example.factoryguard.domain.notification.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService implements
        GetNotificationsUseCase,
        GetNotificationDetailUseCase,
        MarkNotificationReadUseCase,
        MarkAllNotificationsReadUseCase,
        CreateNotificationUseCase {

    private final LoadNotificationPort loadNotificationPort;
    private final SaveNotificationPort saveNotificationPort;

    @Override
    public NotificationPageResult execute(ListNotificationsQuery query) {
        return loadNotificationPort.search(query);
    }

    @Override
    public NotificationDetail execute(Long userId, Long notificationId) {
        Notification notification = loadNotificationPort.findById(notificationId)
                .orElseThrow(NotificationNotFoundException::new);
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_FORBIDDEN);
        }
        return NotificationDetail.builder()
                .notificationId(notification.getNotificationId())
                .notificationType(notification.getNotificationType())
                .severity(notification.getSeverity())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .targetUrl(notification.getTargetUrl())
                .isRead(notification.getIsRead())
                .build();
    }

    @Override
    @Transactional
    public void execute(MarkNotificationReadCommand command) {
        Notification notification = loadNotificationPort.findById(command.getNotificationId())
                .orElseThrow(NotificationNotFoundException::new);
        if (!notification.getUserId().equals(command.getUserId())) {
            throw new BusinessException(ErrorCode.NOTIFICATION_FORBIDDEN);
        }
        if (Boolean.TRUE.equals(notification.getIsRead())) {
            return;
        }
        saveNotificationPort.markAsRead(notification.getNotificationId());
    }

    @Override
    @Transactional
    public MarkAllReadResult execute(Long userId) {
        int updated = saveNotificationPort.markAllAsReadByUserId(userId);
        return MarkAllReadResult.builder().updatedCount(updated).build();
    }

    @Override
    @Transactional
    public Optional<Notification> execute(CreateNotificationCommand command) {
        if (command.getDedupKey() != null && !command.getDedupKey().isBlank()
                && loadNotificationPort.existsByUserIdAndDedupKey(command.getUserId(), command.getDedupKey())) {
            log.debug("Skip duplicate notification, userId={}, dedupKey={}",
                    command.getUserId(), command.getDedupKey());
            return Optional.empty();
        }

        Notification notification = Notification.builder()
                .userId(command.getUserId())
                .notificationType(command.getNotificationType())
                .severity(command.getSeverity())
                .title(command.getTitle())
                .message(command.getMessage())
                .relatedType(command.getRelatedType())
                .relatedId(command.getRelatedId())
                .targetUrl(command.getTargetUrl())
                .dedupKey(command.getDedupKey())
                .isRead(Boolean.FALSE)
                .build();

        return Optional.of(saveNotificationPort.save(notification));
    }
}
