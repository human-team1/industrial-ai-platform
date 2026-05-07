package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.AvailableAnalysisTargetItem;
import com.example.factoryguard.application.port.in.inspection.GetAvailableAnalysisTargetsUseCase;
import com.example.factoryguard.application.port.out.inspection.LoadAnalysisTargetPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisTargetQueryService implements GetAvailableAnalysisTargetsUseCase {

    private final LoadAnalysisTargetPort loadAnalysisTargetPort;

    @Override
    @Transactional(readOnly = true)
    public List<AvailableAnalysisTargetItem> execute(Long organizationId) {
        if (organizationId == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return loadAnalysisTargetPort.findByOrganizationId(organizationId).stream()
                .map(target -> AvailableAnalysisTargetItem.builder()
                        .targetId(target.getTargetId())
                        .targetName(target.getTargetName())
                        .equipmentName(target.getEquipmentName())
                        .productName(target.getProductName())
                        .displayName(buildDisplayName(target.getTargetName(), target.getEquipmentName()))
                        .build())
                .toList();
    }

    private String buildDisplayName(String targetName, String equipmentName) {
        if (targetName != null && !targetName.isBlank()) {
            return targetName;
        }
        if (equipmentName != null && !equipmentName.isBlank()) {
            return equipmentName;
        }
        return "검사 대상";
    }
}
