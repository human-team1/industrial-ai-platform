package com.example.factoryguard.adapter.out.persistence.user.mapper;

import com.example.factoryguard.adapter.out.persistence.user.UserSettingJpaEntity;
import com.example.factoryguard.domain.user.model.UserSetting;
import org.springframework.stereotype.Component;

@Component
public class UserSettingPersistenceMapper {

    public UserSetting toDomain(UserSettingJpaEntity entity) {
        return UserSetting.builder()
                .userSettingId(entity.getUserSettingId())
                .userId(entity.getUserId())
                .notificationEnabled(entity.isNotificationEnabled())
                .defaultDashboardRange(entity.getDefaultDashboardRange())
                .defaultCameraId(entity.getDefaultCameraId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserSettingJpaEntity toNewEntity(UserSetting domain) {
        return UserSettingJpaEntity.builder()
                .userId(domain.getUserId())
                .notificationEnabled(Boolean.TRUE.equals(domain.getNotificationEnabled()))
                .defaultDashboardRange(domain.getDefaultDashboardRange())
                .defaultCameraId(domain.getDefaultCameraId())
                .build();
    }
}
