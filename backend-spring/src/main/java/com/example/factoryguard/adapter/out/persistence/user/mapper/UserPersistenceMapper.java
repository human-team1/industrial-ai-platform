package com.example.factoryguard.adapter.out.persistence.user.mapper;

import com.example.factoryguard.adapter.out.persistence.user.UserJpaEntity;
import com.example.factoryguard.domain.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public User toDomain(UserJpaEntity entity) {
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

    public UserJpaEntity toEntity(User user) {
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
