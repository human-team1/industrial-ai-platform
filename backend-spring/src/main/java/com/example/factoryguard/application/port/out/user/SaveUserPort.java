package com.example.factoryguard.application.port.out.user;

import com.example.factoryguard.domain.user.model.User;

public interface SaveUserPort {

    User save(User user);
}
