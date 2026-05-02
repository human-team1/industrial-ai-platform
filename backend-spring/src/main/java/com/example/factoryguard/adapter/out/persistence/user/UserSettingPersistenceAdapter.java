package com.example.factoryguard.adapter.out.persistence.user;

import com.example.factoryguard.adapter.out.persistence.user.mapper.UserSettingPersistenceMapper;
import com.example.factoryguard.application.port.out.user.LoadUserSettingPort;
import com.example.factoryguard.application.port.out.user.SaveUserSettingPort;
import com.example.factoryguard.domain.user.model.UserSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserSettingPersistenceAdapter implements LoadUserSettingPort, SaveUserSettingPort {

    private final UserSettingJpaRepository userSettingJpaRepository;
    private final UserSettingPersistenceMapper userSettingPersistenceMapper;

    @Override
    public Optional<UserSetting> findByUserId(Long userId) {
        return userSettingJpaRepository.findByUserId(userId)
                .map(userSettingPersistenceMapper::toDomain);
    }

    @Override
    public UserSetting save(UserSetting userSetting) {
        UserSettingJpaEntity saved = userSettingJpaRepository.findByUserId(userSetting.getUserId())
                .map(existing -> {
                    existing.update(
                            Boolean.TRUE.equals(userSetting.getNotificationEnabled()),
                            userSetting.getDefaultDashboardRange(),
                            userSetting.getDefaultCameraId()
                    );
                    return existing;
                })
                .orElseGet(() -> userSettingJpaRepository.save(
                        userSettingPersistenceMapper.toNewEntity(userSetting)
                ));
        return userSettingPersistenceMapper.toDomain(saved);
    }
}
