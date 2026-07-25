package com.commandhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String adminPassword = "changeme";
    private String dataPath = "./data/commands.json";
    private int tokenExpireHours = 24;

    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }

    public String getDataPath() { return dataPath; }
    public void setDataPath(String dataPath) { this.dataPath = dataPath; }

    public int getTokenExpireHours() { return tokenExpireHours; }
    public void setTokenExpireHours(int tokenExpireHours) { this.tokenExpireHours = tokenExpireHours; }
}
