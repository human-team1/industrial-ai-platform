package com.example.factoryguard.adapter.out.persistence.notification;

import com.example.factoryguard.application.dto.notification.ListNotificationsQuery;
import com.example.factoryguard.application.dto.notification.NotificationPageResult;
import com.example.factoryguard.application.dto.notification.NotificationSummary;
import com.example.factoryguard.application.port.out.notification.LoadNotificationPort;
import com.example.factoryguard.application.port.out.notification.SaveNotificationPort;
import com.example.factoryguard.domain.notification.exception.NotificationNotFoundException;
import com.example.factoryguard.domain.notification.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements LoadNotificationPort, SaveNotificationPort {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationPersistenceMapper mapper;

    @Override
    public NotificationPageResult search(ListNotificationsQuery query) {
        PageRequest pageable = PageRequest.of(
                Math.max(0, query.getPage()),
                Math.max(1, query.getSize()),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<NotificationJpaEntity> page = notificationJpaRepository.search(
                query.getUserId(),
                query.getType(),
                query.getIsRead(),
                pageable
        );

        List<NotificationSummary> content = page.getContent().stream()
                .map(mapper::toSummary)
                .toList();

        return NotificationPageResult.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public Optional<Notification> findById(Long notificationId) {
        return notificationJpaRepository.findById(notificationId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserIdAndDedupKey(Long userId, String dedupKey) {
        if (dedupKey == null || dedupKey.isBlank()) {
            return false;
        }
        return notificationJpaRepository.existsByUserIdAndDedupKey(userId, dedupKey);
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity saved = notificationJpaRepository.save(mapper.toEntity(notification));
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        NotificationJpaEntity entity = notificationJpaRepository.findById(notificationId)
                .orElseThrow(NotificationNotFoundException::new);
        if (Boolean.FALSE.equals(entity.getIsRead())) {
            entity.markAsRead();
        }
    }

    @Override
    @Transactional
    public int markAllAsReadByUserId(Long userId) {
        return notificationJpaRepository.markAllAsReadByUserId(userId);
    }
}
