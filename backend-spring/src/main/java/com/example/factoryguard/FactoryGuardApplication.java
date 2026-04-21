package com.example.factoryguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FactoryGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(FactoryGuardApplication.class, args);
    }
}
