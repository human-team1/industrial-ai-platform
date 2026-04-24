package com.example.factoryguard.application.service;

import com.example.factoryguard.application.port.in.SignupUseCase;
import com.example.factoryguard.application.port.in.dto.SignupCommand;
import com.example.factoryguard.application.port.in.dto.SignupResult;
import com.example.factoryguard.application.port.out.UserRepository;
import com.example.factoryguard.domain.user.User;
import com.example.factoryguard.domain.user.exception.UserAlreadyExistsException;

public class SignupService implements SignupUseCase {

    private final UserRepository userRepository;

    public SignupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public SignupResult signup(SignupCommand command) {
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new UserAlreadyExistsException(command.getEmail());
        }

        User user = User.create(command.getEmail(), command.getPassword(), command.getName());
        User savedUser = userRepository.save(user);

        return new SignupResult(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getName(),
            savedUser.getStatus()
        );
    }
}