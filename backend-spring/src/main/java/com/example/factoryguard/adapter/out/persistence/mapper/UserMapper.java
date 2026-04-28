package com.example.factoryguard.adapter.out.persistence.mapper;

import com.example.factoryguard.adapter.out.persistence.entity.UserEntity;
import com.example.factoryguard.domain.user.User;
import com.example.factoryguard.domain.user.UserRole;
import com.example.factoryguard.domain.user.UserStatus;

public class UserMapper {

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
            entity.getId(),
            entity.getEmail(),
            entity.getPassword(),
            entity.getName(),
            entity.getGoogleSub(),
            entity.getPicture(),
            entity.getCompany(),
            entity.getPosition(),
            entity.getPhone(),
            UserRole.valueOf(entity.getRole()),
            UserStatus.valueOf(entity.getStatus()),
            entity.getCreatedAt(),  
            entity.getUpdatedAt()   
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
        entity.setGoogleSub(user.getGoogleSub());
        entity.setPicture(user.getPicture());
        entity.setCompany(user.getCompany());
        entity.setPosition(user.getPosition());
        entity.setPhone(user.getPhone());
        entity.setRole(user.getRole().getValue());
        entity.setStatus(user.getStatus().getValue());
        entity.setCreatedAt(user.getCreatedAt()); 
        entity.setUpdatedAt(user.getUpdatedAt()); 
        return entity;
    }
}