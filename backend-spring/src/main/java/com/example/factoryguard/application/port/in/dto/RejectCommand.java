package com.example.factoryguard.application.port.in.dto;

public class RejectCommand {
    private final Long userId;
    private final Long adminId;
    private final String reason;

    public RejectCommand(Long userId, Long adminId, String reason) {
        this.userId = userId;
        this.adminId = adminId;
        this.reason = reason;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public String getReason() {
        return reason;
    }
}