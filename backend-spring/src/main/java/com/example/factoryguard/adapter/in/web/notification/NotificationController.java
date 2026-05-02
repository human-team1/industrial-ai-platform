package com.example.factoryguard.adapter.in.web.notification;

import com.example.factoryguard.adapter.in.web.notification.response.MarkAllReadResponse;
import com.example.factoryguard.adapter.in.web.notification.response.NotificationDetailResponse;
import com.example.factoryguard.adapter.in.web.notification.response.NotificationListResponse;
import com.example.factoryguard.application.dto.notification.ListNotificationsQuery;
import com.example.factoryguard.application.dto.notification.MarkAllReadResult;
import com.example.factoryguard.application.dto.notification.MarkNotificationReadCommand;
import com.example.factoryguard.application.dto.notification.NotificationDetail;
import com.example.factoryguard.application.dto.notification.NotificationPageResult;
import com.example.factoryguard.application.port.in.notification.GetNotificationDetailUseCase;
import com.example.factoryguard.application.port.in.notification.GetNotificationsUseCase;
import com.example.factoryguard.application.port.in.notification.MarkAllNotificationsReadUseCase;
import com.example.factoryguard.application.port.in.notification.MarkNotificationReadUseCase;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.SecurityUtils;
import com.example.factoryguard.domain.notification.vo.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final GetNotificationDetailUseCase getNotificationDetailUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;
    private final MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = securityUtils.getCurrentUserId();
        NotificationType parsedType = parseType(type);

        ListNotificationsQuery query = new ListNotificationsQuery(userId, parsedType, isRead, page, size);
        NotificationPageResult result = getNotificationsUseCase.execute(query);
        return ResponseEntity.ok(
                ApiResponse.success(NotificationListResponse.from(result), "알림 목록을 조회했습니다.")
        );
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationDetailResponse>> detail(@PathVariable Long notificationId) {
        Long userId = securityUtils.getCurrentUserId();
        NotificationDetail detail = getNotificationDetailUseCase.execute(userId, notificationId);
        return ResponseEntity.ok(
                ApiResponse.success(NotificationDetailResponse.from(detail), "알림 상세를 조회했습니다.")
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long notificationId) {
        Long userId = securityUtils.getCurrentUserId();
        markNotificationReadUseCase.execute(new MarkNotificationReadCommand(userId, notificationId));
        return ResponseEntity.ok(ApiResponse.success(null, "알림을 읽음 처리했습니다."));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<MarkAllReadResponse>> markAllAsRead() {
        Long userId = securityUtils.getCurrentUserId();
        MarkAllReadResult result = markAllNotificationsReadUseCase.execute(userId);
        return ResponseEntity.ok(
                ApiResponse.success(MarkAllReadResponse.from(result), "전체 알림을 읽음 처리했습니다.")
        );
    }

    private NotificationType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return NotificationType.valueOf(type);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.NOTIFICATION_INVALID_TYPE);
        }
    }
}
