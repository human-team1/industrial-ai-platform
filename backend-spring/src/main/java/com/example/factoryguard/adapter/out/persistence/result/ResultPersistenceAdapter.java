package com.example.factoryguard.adapter.out.persistence.result;

import com.example.factoryguard.application.port.out.result.LoadAnomalyRegionPort;
import com.example.factoryguard.application.port.out.result.LoadResultArtifactPort;
import com.example.factoryguard.application.port.out.result.LoadResultImagePort;
import com.example.factoryguard.application.port.out.result.SaveAnomalyRegionPort;
import com.example.factoryguard.application.port.out.result.SaveResultArtifactPort;
import com.example.factoryguard.application.port.out.result.SaveResultImagePort;
import com.example.factoryguard.adapter.out.persistence.result.mapper.ResultPersistenceMapper;
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
    private final ResultPersistenceMapper resultPersistenceMapper;

    @Override
    public ResultArtifact save(ResultArtifact artifact) {
        ResultArtifactJpaEntity saved = resultArtifactJpaRepository.save(resultPersistenceMapper.toEntity(artifact));
        return resultPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<ResultArtifact> findArtifactsByResultId(Long resultId) {
        return resultArtifactJpaRepository.findAllByResultIdOrderByCreatedAtAsc(resultId).stream()
                .map(resultPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Image save(Image image) {
        ImageJpaEntity saved = imageJpaRepository.save(resultPersistenceMapper.toEntity(image));
        return resultPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<Image> findImagesByResultId(Long resultId) {
        return imageJpaRepository.findAllByResultIdOrderByCreatedAtAsc(resultId).stream()
                .map(resultPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public AnomalyRegion save(AnomalyRegion region) {
        AnomalyRegionJpaEntity saved = anomalyRegionJpaRepository.save(resultPersistenceMapper.toEntity(region));
        return resultPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<AnomalyRegion> findAllByImageId(Long imageId) {
        return anomalyRegionJpaRepository.findAllByImageIdOrderByCreatedAtAsc(imageId).stream()
                .map(resultPersistenceMapper::toDomain)
                .toList();
    }
}
