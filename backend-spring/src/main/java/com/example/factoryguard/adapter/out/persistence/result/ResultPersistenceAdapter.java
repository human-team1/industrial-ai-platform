package com.example.factoryguard.adapter.out.persistence.result;

import com.example.factoryguard.application.port.out.result.LoadAnomalyRegionPort;
import com.example.factoryguard.application.port.out.result.LoadResultArtifactPort;
import com.example.factoryguard.application.port.out.result.LoadResultImagePort;
import com.example.factoryguard.application.port.out.result.SaveAnomalyRegionPort;
import com.example.factoryguard.application.port.out.result.SaveResultArtifactPort;
import com.example.factoryguard.application.port.out.result.SaveResultImagePort;
import com.example.factoryguard.domain.result.model.AnomalyRegion;
import com.example.factoryguard.domain.result.model.Image;
import com.example.factoryguard.domain.result.model.ResultArtifact;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResultPersistenceAdapter implements
        SaveResultArtifactPort, LoadResultArtifactPort,
        SaveResultImagePort, LoadResultImagePort,
        SaveAnomalyRegionPort, LoadAnomalyRegionPort {

    private final ResultArtifactJpaRepository resultArtifactJpaRepository;
    private final ImageJpaRepository imageJpaRepository;
    private final AnomalyRegionJpaRepository anomalyRegionJpaRepository;

    @Override
    public ResultArtifact save(ResultArtifact artifact) {
        ResultArtifactJpaEntity saved = resultArtifactJpaRepository.save(
                ResultArtifactJpaEntity.builder()
                        .resultId(artifact.getResultId())
                        .artifactType(artifact.getArtifactType())
                        .fileId(artifact.getFileId())
                        .build()
        );
        return toDomain(saved);
    }

    @Override
    public List<ResultArtifact> findArtifactsByResultId(Long resultId) {
        return resultArtifactJpaRepository.findAllByResultIdOrderByCreatedAtAsc(resultId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Image save(Image image) {
        ImageJpaEntity saved = imageJpaRepository.save(
                ImageJpaEntity.builder()
                        .resultId(image.getResultId())
                        .fileId(image.getFileId())
                        .imageRole(image.getImageRole())
                        .build()
        );
        return toDomain(saved);
    }

    @Override
    public List<Image> findImagesByResultId(Long resultId) {
        return imageJpaRepository.findAllByResultIdOrderByCreatedAtAsc(resultId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public AnomalyRegion save(AnomalyRegion region) {
        AnomalyRegionJpaEntity saved = anomalyRegionJpaRepository.save(
                AnomalyRegionJpaEntity.builder()
                        .imageId(region.getImageId())
                        .labelCode(region.getLabelCode())
                        .bboxX(region.getBboxX())
                        .bboxY(region.getBboxY())
                        .bboxW(region.getBboxW())
                        .bboxH(region.getBboxH())
                        .score(region.getScore())
                        .build()
        );
        return toDomain(saved);
    }

    @Override
    public List<AnomalyRegion> findAllByImageId(Long imageId) {
        return anomalyRegionJpaRepository.findAllByImageIdOrderByCreatedAtAsc(imageId).stream()
                .map(this::toDomain)
                .toList();
    }

    private ResultArtifact toDomain(ResultArtifactJpaEntity e) {
        return ResultArtifact.builder()
                .artifactId(e.getArtifactId())
                .resultId(e.getResultId())
                .artifactType(e.getArtifactType())
                .fileId(e.getFileId())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private Image toDomain(ImageJpaEntity e) {
        return Image.builder()
                .imageId(e.getImageId())
                .resultId(e.getResultId())
                .fileId(e.getFileId())
                .imageRole(e.getImageRole())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private AnomalyRegion toDomain(AnomalyRegionJpaEntity e) {
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
