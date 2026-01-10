package com.transaction.training.service;

import com.transaction.training.config.DataSourceContextHolder;
import com.transaction.training.dto.DatabaseInfo;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseManagementService {
    
    private final DataSource dataSource;
    
    @Value("${spring.profiles.active:mysql}")
    private String activeProfile;
    
    public List<String> getAvailableProfiles() {
        return Arrays.asList("mysql", "oracle");
    }
    
    public DatabaseInfo getActiveDatabaseInfo() {
        DatabaseInfo info = DatabaseInfo.builder()
                .activeProfile(activeProfile)
                .availableProfiles(getAvailableProfiles())
                .build();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            info.setDatabaseType(metaData.getDatabaseProductName());
            info.setJdbcUrl(metaData.getURL());
            info.setDriverClassName(metaData.getDriverName());
            info.setConnected(true);
            
            // Get connection pool info if using HikariCP
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
                
                if (poolMXBean != null) {
                    DatabaseInfo.ConnectionPoolInfo poolInfo = DatabaseInfo.ConnectionPoolInfo.builder()
                            .activeConnections(poolMXBean.getActiveConnections())
                            .idleConnections(poolMXBean.getIdleConnections())
                            .totalConnections(poolMXBean.getTotalConnections())
                            .maximumPoolSize(hikariDataSource.getMaximumPoolSize())
                            .build();
                    info.setPoolInfo(poolInfo);
                }
            }
            
            log.info("Database info retrieved: {} ({})", info.getDatabaseType(), info.getActiveProfile());
            
        } catch (Exception e) {
            log.error("Error getting database info", e);
            info.setConnected(false);
        }
        
        return info;
    }
    
    public boolean switchDatabase(String profile) {
        try {
            if (!getAvailableProfiles().contains(profile)) {
                log.warn("Invalid database profile: {}", profile);
                return false;
            }
            
            // Set the datasource context
            DataSourceContextHolder.setDataSourceType(profile);
            this.activeProfile = profile;
            
            // Verify connection
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                log.info("Switched to database: {} - {}", 
                        metaData.getDatabaseProductName(), 
                        metaData.getURL());
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error switching database to profile: {}", profile, e);
            return false;
        }
    }
    
    public Map<String, Object> checkDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            status.put("connected", true);
            status.put("databaseType", metaData.getDatabaseProductName());
            status.put("databaseVersion", metaData.getDatabaseProductVersion());
            status.put("driverName", metaData.getDriverName());
            status.put("driverVersion", metaData.getDriverVersion());
            status.put("url", metaData.getURL());
            status.put("userName", metaData.getUserName());
            status.put("readOnly", connection.isReadOnly());
            status.put("autoCommit", connection.getAutoCommit());
            status.put("transactionIsolation", getIsolationLevelName(connection.getTransactionIsolation()));
            
            // Pool statistics
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
                
                if (poolMXBean != null) {
                    Map<String, Object> poolStats = new HashMap<>();
                    poolStats.put("activeConnections", poolMXBean.getActiveConnections());
                    poolStats.put("idleConnections", poolMXBean.getIdleConnections());
                    poolStats.put("totalConnections", poolMXBean.getTotalConnections());
                    poolStats.put("threadsAwaitingConnection", poolMXBean.getThreadsAwaitingConnection());
                    poolStats.put("maximumPoolSize", hikariDataSource.getMaximumPoolSize());
                    poolStats.put("minimumIdle", hikariDataSource.getMinimumIdle());
                    status.put("connectionPool", poolStats);
                }
            }
            
        } catch (Exception e) {
            log.error("Error checking database status", e);
            status.put("connected", false);
            status.put("error", e.getMessage());
        }
        
        return status;
    }
    
    private String getIsolationLevelName(int level) {
        return switch (level) {
            case Connection.TRANSACTION_NONE -> "NONE";
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "READ_UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED -> "READ_COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE_READ";
            case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
            default -> "UNKNOWN";
        };
    }
}
