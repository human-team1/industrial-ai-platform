package com.example.factoryguard.adapter.out.persistence.signuprequest;

import com.example.factoryguard.adapter.out.persistence.user.UserJpaEntity;
import com.example.factoryguard.adapter.out.persistence.user.UserJpaRepository;
import com.example.factoryguard.application.dto.signup.SignupRequestSummary;
import com.example.factoryguard.application.port.out.signuprequest.FindPendingSignupRequestsPort;
import com.example.factoryguard.application.port.out.signuprequest.ProcessSignupRequestPort;
import com.example.factoryguard.application.port.out.signuprequest.SaveSignupRequestPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.SignupRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SignupRequestPersistenceAdapter
        implements SaveSignupRequestPort, FindPendingSignupRequestsPort, ProcessSignupRequestPort {

    private final SignupRequestJpaRepository signupRequestJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public void saveSignupRequest(Long userId) {
        signupRequestJpaRepository.save(SignupRequestJpaEntity.builder()
                .userId(userId)
                .build());
    }

    @Override
    public List<SignupRequestSummary> findPending() {
        List<SignupRequestJpaEntity> requests =
                signupRequestJpaRepository.findAllByRequestStatus(SignupRequestStatus.PENDING);

        List<Long> userIds = requests.stream()
                .map(SignupRequestJpaEntity::getUserId)
                .collect(Collectors.toList());

        Map<Long, UserJpaEntity> userMap = userJpaRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserJpaEntity::getUserId, u -> u));

        return requests.stream()
                .map(r -> {
                    UserJpaEntity user = userMap.get(r.getUserId());
                    return SignupRequestSummary.builder()
                            .requestId(r.getRequestId())
                            .userId(r.getUserId())
                            .name(user != null ? user.getName() : "")
                            .email(user != null ? user.getEmail() : "")
                            .picture(user != null ? user.getPicture() : null)
                            .requestedAt(r.getRequestedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long approve(Long requestId, Long adminUserId) {
        SignupRequestJpaEntity entity = signupRequestJpaRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        entity.approve(adminUserId);
        return entity.getUserId();
    }

    @Override
    public Long reject(Long requestId, Long adminUserId, String rejectReason) {
        SignupRequestJpaEntity entity = signupRequestJpaRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        entity.reject(adminUserId, rejectReason);
        return entity.getUserId();
    }
}
