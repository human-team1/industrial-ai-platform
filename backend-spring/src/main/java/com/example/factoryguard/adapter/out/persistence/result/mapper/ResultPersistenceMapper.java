package com.example.factoryguard.adapter.out.persistence.result.mapper;

import com.example.factoryguard.adapter.out.persistence.result.AnomalyRegionJpaEntity;
import com.example.factoryguard.adapter.out.persistence.result.ImageJpaEntity;
import com.example.factoryguard.adapter.out.persistence.result.ResultArtifactJpaEntity;
import com.example.factoryguard.domain.result.model.AnomalyRegion;
import com.example.factoryguard.domain.result.model.Image;
import com.example.factoryguard.domain.result.model.ResultArtifact;
import org.springframework.stereotype.Component;

@Component
public class ResultPersistenceMapper {

    public ResultArtifactJpaEntity toEntity(ResultArtifact artifact) {
        return ResultArtifactJpaEntity.builder()
                .resultId(artifact.getResultId())
                .artifactType(artifact.getArtifactType())
                .fileId(artifact.getFileId())
                .build();
    }

    public ResultArtifact toDomain(ResultArtifactJpaEntity e) {
        return ResultArtifact.builder()
                .artifactId(e.getArtifactId())
                .resultId(e.getResultId())
                .artifactType(e.getArtifactType())
                .fileId(e.getFileId())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public ImageJpaEntity toEntity(Image image) {
        return ImageJpaEntity.builder()
                .resultId(image.getResultId())
                .fileId(image.getFileId())
                .imageRole(image.getImageRole())
                .build();
    }

    public Image toDomain(ImageJpaEntity e) {
        return Image.builder()
                .imageId(e.getImageId())
                .resultId(e.getResultId())
                .fileId(e.getFileId())
                .imageRole(e.getImageRole())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public AnomalyRegionJpaEntity toEntity(AnomalyRegion region) {
        return AnomalyRegionJpaEntity.builder()
                .imageId(region.getImageId())
                .labelCode(region.getLabelCode())
                .bboxX(region.getBboxX())
                .bboxY(region.getBboxY())
                .bboxW(region.getBboxW())
                .bboxH(region.getBboxH())
                .score(region.getScore())
                .build();
    }

    public AnomalyRegion toDomain(AnomalyRegionJpaEntity e) {
        return AnomalyRegion.builder()
                .regionId(e.getRegionId())
                .imageId(e.getImageId())
                .labelCode(e.getLabelCode())
                .bboxX(e.getBboxX())
                .bboxY(e.getBboxY())
                .bboxW(e.getBboxW())
                .bboxH(e.getBboxH())
                .score(e.getScore())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
