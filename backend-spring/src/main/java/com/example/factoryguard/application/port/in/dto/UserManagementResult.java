package com.example.factoryguard.application.port.in.dto;

import com.example.factoryguard.domain.user.UserStatus;

public class UserManagementResult {
    private final Long userId;
    private final String email;
    private final String name;
    private final UserStatus status;
    private final String processedAt;
    private final Long processedBy;

    public UserManagementResult(Long userId, String email, String name, 
                               UserStatus status, String processedAt, Long processedBy) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.status = status;
        this.processedAt = processedAt;
        this.processedBy = processedBy;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    public Long getProcessedBy() {
        return processedBy;
    }
}