package com.example.factoryguard.adapter.out.persistence.result;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "result_id", nullable = false)
    private Long resultId;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "image_role")
    private String imageRole;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
