package com.example.factoryguard.config.persistence;

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class MyBatisConfig {

    @Bean
    public ConfigurationCustomizer myBatisConfigurationCustomizer() {
        return (Configuration configuration) -> configuration.setMapUnderscoreToCamelCase(true);
    }
}
