package com.example.factoryguard.adapter.out.persistence.mapper;

import com.example.factoryguard.adapter.out.persistence.entity.UserEntity;
import com.example.factoryguard.domain.user.User;
import com.example.factoryguard.domain.user.UserRole;
import com.example.factoryguard.domain.user.UserStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
            entity.getId(),
            entity.getEmail(),
            entity.getPassword(),
            entity.getName(),
            UserRole.valueOf(entity.getRole()),
            UserStatus.valueOf(entity.getStatus()),
            LocalDateTime.parse(entity.getCreatedAt(), FORMATTER),
            LocalDateTime.parse(entity.getUpdatedAt(), FORMATTER)
        );
    }

    public static UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        if (user.getId() != null) {
            entity.setId(user.getId());
        }
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setName(user.getName());
        entity.setRole(user.getRole().getValue());
        entity.setStatus(user.getStatus().getValue());
        entity.setCreatedAt(user.getCreatedAt().format(FORMATTER));
        entity.setUpdatedAt(user.getUpdatedAt().format(FORMATTER));
        return entity;
    }
}