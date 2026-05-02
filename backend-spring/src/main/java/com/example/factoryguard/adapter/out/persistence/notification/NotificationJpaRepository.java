package com.example.factoryguard.adapter.out.persistence.notification;

import com.example.factoryguard.domain.notification.vo.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {

    @Query("""
            SELECT n FROM NotificationJpaEntity n
            WHERE n.userId = :userId
              AND (:type IS NULL OR n.notificationType = :type)
              AND (:isRead IS NULL OR n.isRead = :isRead)
            """)
    Page<NotificationJpaEntity> search(@Param("userId") Long userId,
                                       @Param("type") NotificationType type,
                                       @Param("isRead") Boolean isRead,
                                       Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE NotificationJpaEntity n
               SET n.isRead = true
             WHERE n.userId = :userId
               AND n.isRead = false
            """)
    int markAllAsReadByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndDedupKey(Long userId, String dedupKey);
}
