package com.example.factoryguard.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MariaDBContainer;

public class MariaDbContainerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final MariaDBContainer<?> CONTAINER;

    static {
        CONTAINER = new MariaDBContainer<>("mariadb:10.11")
                .withDatabaseName("industrial_ai_test")
                .withUsername("test_user")
                .withPassword("test_password")
                .withInitScript("init/industrial-ai-platform.sql");
        CONTAINER.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url=" + CONTAINER.getJdbcUrl(),
                "spring.datasource.username=" + CONTAINER.getUsername(),
                "spring.datasource.password=" + CONTAINER.getPassword(),
                "spring.datasource.driver-class-name=org.mariadb.jdbc.Driver"
        ).applyTo(applicationContext.getEnvironment());
    }
}
