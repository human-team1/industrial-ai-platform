package com.example.factoryguard.application.port.out.result;

import com.example.factoryguard.domain.result.model.Image;

public interface SaveResultImagePort {

    Image save(Image image);
}
