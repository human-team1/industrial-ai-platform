package com.example.factoryguard.adapter.out.persistence.user;

import com.example.factoryguard.domain.user.model.UserRole;
import com.example.factoryguard.domain.user.model.UserStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "google_sub", unique = true)
    private String googleSub;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "picture")
    private String picture;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void updateStatus(UserStatus newStatus) {
        this.status = newStatus;
    }

    public void updateProfile(String name, String phone) {
        if (name != null) {
            this.name = name;
        }
        this.phone = phone;
    }

    @Builder
    public UserJpaEntity(Long organizationId, String googleSub, String email,
                         String passwordHash, String name, String picture,
                         String phone, UserStatus status, UserRole role) {
        this.organizationId = organizationId;
        this.googleSub = googleSub;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.picture = picture;
        this.phone = phone;
        this.status = status;
        this.role = role;
    }
}
