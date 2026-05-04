package com.example.factoryguard.application.service.notification;

import com.example.factoryguard.application.dto.notification.MarkNotificationReadCommand;
import com.example.factoryguard.application.port.out.notification.LoadNotificationPort;
import com.example.factoryguard.application.port.out.notification.SaveNotificationPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.notification.exception.NotificationNotFoundException;
import com.example.factoryguard.domain.notification.model.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private LoadNotificationPort loadNotificationPort;
    @Mock private SaveNotificationPort saveNotificationPort;

    @InjectMocks
    private NotificationService service;

    @Test
    @DisplayName("No.38 알림 단건 읽음 처리 - 미읽음 알림에 markAsRead Port 호출")
    void marksUnreadNotificationAsRead() {
        long notificationId = 1L;
        long userId = 1L;
        Notification unread = Notification.builder()
                .notificationId(notificationId).userId(userId).isRead(Boolean.FALSE).build();
        when(loadNotificationPort.findById(notificationId)).thenReturn(Optional.of(unread));

        service.execute(new MarkNotificationReadCommand(userId, notificationId));

        verify(saveNotificationPort).markAsRead(notificationId);
    }

    @Test
    @DisplayName("No.38 이미 읽은 알림 - markAsRead 호출 생략(no-op)")
    void skipsAlreadyReadNotification() {
        long notificationId = 2L;
        long userId = 1L;
        Notification alreadyRead = Notification.builder()
                .notificationId(notificationId).userId(userId).isRead(Boolean.TRUE).build();
        when(loadNotificationPort.findById(notificationId)).thenReturn(Optional.of(alreadyRead));

        service.execute(new MarkNotificationReadCommand(userId, notificationId));

        verify(saveNotificationPort, never()).markAsRead(eq(notificationId));
    }

    @Test
    @DisplayName("No.38 다른 사용자 알림 - NOTIFICATION_FORBIDDEN")
    void rejectsCrossUserAccess() {
        long notificationId = 3L;
        Notification othersNotification = Notification.builder()
                .notificationId(notificationId).userId(99L).isRead(Boolean.FALSE).build();
        when(loadNotificationPort.findById(notificationId)).thenReturn(Optional.of(othersNotification));

        assertThatThrownBy(() -> service.execute(new MarkNotificationReadCommand(1L, notificationId)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOTIFICATION_FORBIDDEN);

        verify(saveNotificationPort, never()).markAsRead(eq(notificationId));
    }

    @Test
    @DisplayName("No.38 존재하지 않는 알림 - NotificationNotFoundException")
    void rejectsMissingNotification() {
        when(loadNotificationPort.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new MarkNotificationReadCommand(1L, 999L)))
                .isInstanceOf(NotificationNotFoundException.class);
    }
}
