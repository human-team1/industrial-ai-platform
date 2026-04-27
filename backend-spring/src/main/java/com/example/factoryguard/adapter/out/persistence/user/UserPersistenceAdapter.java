package com.example.factoryguard.adapter.out.persistence.user;

import com.example.factoryguard.application.port.out.user.FindUserByGoogleSubPort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.SaveUserPort;
import com.example.factoryguard.application.port.out.user.UpdateUserStatusPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements FindUserByGoogleSubPort, FindUserByIdPort, SaveUserPort, UpdateUserStatusPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findByGoogleSub(String googleSub) {
        return userJpaRepository.findByGoogleSub(googleSub)
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId)
                .map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity saved = userJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void updateStatus(Long userId, UserStatus status) {
        UserJpaEntity entity = userJpaRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        entity.updateStatus(status);
    }

    private User toDomain(UserJpaEntity entity) {
        return User.builder()
                .userId(entity.getUserId())
                .organizationId(entity.getOrganizationId())
                .googleSub(entity.getGoogleSub())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .name(entity.getName())
                .picture(entity.getPicture())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .role(entity.getRole())
                .lastLoginAt(entity.getLastLoginAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    private UserJpaEntity toEntity(User user) {
        return UserJpaEntity.builder()
                .organizationId(user.getOrganizationId())
                .googleSub(user.getGoogleSub())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .name(user.getName())
                .picture(user.getPicture())
                .phone(user.getPhone())
                .status(user.getStatus())
                .role(user.getRole())
                .build();
    }
}
