package com.example.factoryguard.application.service;

import com.example.factoryguard.application.port.in.SignupUseCase;
import com.example.factoryguard.application.port.in.dto.SignupCommand;
import com.example.factoryguard.application.port.in.dto.SignupResult;
import com.example.factoryguard.application.port.out.UserRepository;
import com.example.factoryguard.domain.user.User;
import com.example.factoryguard.domain.user.exception.UserAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SignupService implements SignupUseCase {

    private final UserRepository userRepository;

    public SignupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public SignupResult signup(SignupCommand command) {
        // 1. Google Sub 검증 (KAN-126 관련)
        validateGoogleAuth(command.getGoogleSub());

        // 2. 중복 가입 방지 로직 (KAN-128 핵심 요구사항)
        checkDuplicateUser(command.getEmail(), command.getGoogleSub());

        // 3. 도메인 엔티티 생성 (PENDING 상태로 생성됨)
        User user = User.create(
            command.getEmail(),
            command.getPassword(),
            command.getName(),
            command.getGoogleSub(),
            command.getPicture(),
            command.getCompany(),
            command.getPosition(),
            command.getPhone()
        );

        // 4. 영속성 포트를 통한 저장
        User savedUser = userRepository.save(user);

        // 5. 결과 반환 (Result DTO 변환)
        return new SignupResult(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getName(),
            savedUser.getPicture(),
            savedUser.getStatus().getValue()
        );
    }

    private void validateGoogleAuth(String googleSub) {
        if (googleSub == null || googleSub.isBlank()) {
            throw new IllegalArgumentException("Google 인증 정보(sub)가 누락되었습니다.");
        }
    }

    private void checkDuplicateUser(String email, String googleSub) {
        // Google Sub 중복 검증
        if (userRepository.existsByGoogleSub(googleSub)) {
            throw UserAlreadyExistsException.byGoogleSub(googleSub);
        }

        // 이메일 중복 검증
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }
    }
}