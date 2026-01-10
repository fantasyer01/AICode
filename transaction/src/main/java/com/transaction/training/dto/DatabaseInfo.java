package com.transaction.training.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseInfo {
    
    private String activeProfile;
    private String databaseType;
    private String jdbcUrl;
    private String driverClassName;
    private Boolean connected;
    private ConnectionPoolInfo poolInfo;
    private List<String> availableProfiles;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionPoolInfo {
        private Integer activeConnections;
        private Integer idleConnections;
        private Integer maximumPoolSize;
        private Integer totalConnections;
    }
}
