package com.example.factoryguard.adapter.out.persistence.user;

import com.example.factoryguard.application.port.out.user.FindUserByGoogleSubPort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.SaveUserPort;
import com.example.factoryguard.application.port.out.user.UpdateUserStatusPort;
import com.example.factoryguard.adapter.out.persistence.user.mapper.UserPersistenceMapper;
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
    private final UserPersistenceMapper userPersistenceMapper;

    @Override
    public Optional<User> findByGoogleSub(String googleSub) {
        return userJpaRepository.findByGoogleSub(googleSub)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = userPersistenceMapper.toEntity(user);
        UserJpaEntity saved = userJpaRepository.save(entity);
        return userPersistenceMapper.toDomain(saved);
    }

    @Override
    public void updateStatus(Long userId, UserStatus status) {
        UserJpaEntity entity = userJpaRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        entity.updateStatus(status);
    }
}
