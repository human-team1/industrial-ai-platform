package com.example.factoryguard.domain.user;

import java.time.LocalDateTime;

public class User {
    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final UserRole role;
    private final UserStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public User(Long id, String email, String password, String name, 
                UserRole role, UserStatus status, 
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static User create(String email, String password, String name) {
        LocalDateTime now = LocalDateTime.now();
        return new User(
            null, email, password, name,
            UserRole.USER, UserStatus.PENDING,
            now, now
        );
    }

    public User approve() {
        return new User(id, email, password, name, role, UserStatus.APPROVED, createdAt, LocalDateTime.now());
    }

    public User reject() {
        return new User(id, email, password, name, role, UserStatus.REJECTED, createdAt, LocalDateTime.now());
    }
}