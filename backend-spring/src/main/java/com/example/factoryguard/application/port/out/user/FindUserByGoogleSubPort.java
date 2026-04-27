package com.example.factoryguard.application.port.out.user;

import com.example.factoryguard.domain.user.model.User;

import java.util.Optional;

public interface FindUserByGoogleSubPort {

    Optional<User> findByGoogleSub(String googleSub);
}
