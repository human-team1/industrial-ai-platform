package com.example.factoryguard.adapter.out.persistence.signuprequest;

import com.example.factoryguard.domain.user.model.SignupRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SignupRequestJpaRepository extends JpaRepository<SignupRequestJpaEntity, Long> {

    List<SignupRequestJpaEntity> findAllByRequestStatus(SignupRequestStatus status);

    Optional<SignupRequestJpaEntity> findTopByUserIdAndRequestStatusOrderByProcessedAtDescRequestedAtDesc(
            Long userId, SignupRequestStatus status);
}
