package com.example.factoryguard.adapter.out.persistence.result;

import com.example.factoryguard.domain.result.vo.ImageRole;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "image_role")
    private ImageRole imageRole;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ImageJpaEntity(Long resultId, Long fileId, ImageRole imageRole) {
        this.resultId = resultId;
        this.fileId = fileId;
        this.imageRole = imageRole;
    }
}
