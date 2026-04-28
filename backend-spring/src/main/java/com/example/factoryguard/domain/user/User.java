package com.example.factoryguard.domain.user;

import java.time.LocalDateTime;

public class User {
    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final String googleSub;
    private final String picture;
    private final String company;
    private final String position;
    private final String phone;
    private final UserRole role;
    private final UserStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public User(Long id, String email, String password, String name,
                String googleSub, String picture, String company, String position, String phone,
                UserRole role, UserStatus status,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.googleSub = googleSub;
        this.picture = picture;
        this.company = company;
        this.position = position;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getGoogleSub() { return googleSub; }
    public String getPicture() { return picture; }
    public String getCompany() { return company; }
    public String getPosition() { return position; }
    public String getPhone() { return phone; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static User create(String email, String password, String name, String googleSub,
                              String picture, String company, String position, String phone) {
        LocalDateTime now = LocalDateTime.now();
        return new User(
            null, email, password, name, googleSub, picture, company, position, phone,
            UserRole.USER, UserStatus.PENDING,
            now, now
        );
    }

    public User approve() {
        return new User(
            id, email, password, name, googleSub, picture, company, position, phone,
            role, UserStatus.APPROVED, createdAt, LocalDateTime.now()
        );
    }

    public User reject() {
        return new User(
            id, email, password, name, googleSub, picture, company, position, phone,
            role, UserStatus.REJECTED, createdAt, LocalDateTime.now()
        );
    }
}