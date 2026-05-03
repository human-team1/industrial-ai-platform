package com.example.factoryguard.config.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.session")
public class ActiveSessionProperties {

    private int maxActiveUsers = 3;
    private int activeSessionTtlSeconds = 1800;

    public int getMaxActiveUsers() {
        return maxActiveUsers < 1 ? 3 : maxActiveUsers;
    }

    public void setMaxActiveUsers(int maxActiveUsers) {
        this.maxActiveUsers = maxActiveUsers;
    }

    public int getActiveSessionTtlSeconds() {
        return activeSessionTtlSeconds < 60 ? 1800 : activeSessionTtlSeconds;
    }

    public void setActiveSessionTtlSeconds(int activeSessionTtlSeconds) {
        this.activeSessionTtlSeconds = activeSessionTtlSeconds;
    }
}
