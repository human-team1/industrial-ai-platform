package com.example.factoryguard.application.service.inspection;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "inspection.decision")
public class DecisionProperties {

    private double boundaryMargin = 0.05;
}
