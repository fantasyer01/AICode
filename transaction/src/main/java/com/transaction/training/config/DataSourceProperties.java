package com.transaction.training.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app.datasource")
public class DataSourceProperties {
    
    private String activeProfile = "mysql";
    
    private Map<String, DatabaseProfile> profiles = new HashMap<>();
    
    @Data
    public static class DatabaseProfile {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private HikariProperties hikari = new HikariProperties();
        private String jpaDialect;
    }
    
    @Data
    public static class HikariProperties {
        private int maximumPoolSize = 10;
        private int minimumIdle = 5;
        private long connectionTimeout = 30000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
        private String poolName = "TxDemoHikariPool";
    }
}
