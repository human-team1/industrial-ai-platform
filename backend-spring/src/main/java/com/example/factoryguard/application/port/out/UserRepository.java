package com.example.factoryguard.application.port.out;

import com.example.factoryguard.domain.user.User;
import com.example.factoryguard.domain.user.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    User save(User user);
    List<User> findByStatus(UserStatus status);
    boolean existsByEmail(String email);
}