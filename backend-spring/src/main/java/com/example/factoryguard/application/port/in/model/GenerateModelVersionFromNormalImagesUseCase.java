package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.GenerateModelVersionFromNormalImagesCommand;
import com.example.factoryguard.application.dto.model.GenerateModelVersionsFromNormalImagesResponse;

public interface GenerateModelVersionFromNormalImagesUseCase {

    GenerateModelVersionsFromNormalImagesResponse generateFromNormalImages(GenerateModelVersionFromNormalImagesCommand command);
}
