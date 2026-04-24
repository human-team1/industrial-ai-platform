package com.example.factoryguard.application.port.in.dto;

public class ApproveCommand {
    private final Long userId;
    private final Long adminId;

    public ApproveCommand(Long userId, Long adminId) {
        this.userId = userId;
        this.adminId = adminId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getAdminId() {
        return adminId;
    }
}