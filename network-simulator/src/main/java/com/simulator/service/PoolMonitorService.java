package com.simulator.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.simulator.model.PoolStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class PoolMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(PoolMonitorService.class);

    private final DataSource dataSource;

    public PoolMonitorService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public PoolStatus getPoolStatus() {
        PoolStatus status = new PoolStatus();
        
        if (dataSource instanceof DruidDataSource druid) {
            try {
                status.setActiveCount(druid.getActiveCount());
                status.setPoolingCount(druid.getPoolingCount());
                status.setWaitThreadCount(druid.getWaitThreadCount());
                status.setConnectCount(druid.getConnectCount());
                status.setCloseCount(druid.getCloseCount());
                status.setConnectErrorCount(druid.getConnectErrorCount());
                status.setErrorCount(druid.getErrorCount());
                status.setMaxActive(druid.getMaxActive());
                status.setMinIdle(druid.getMinIdle());
                status.setInitialSize(druid.getInitialSize());
                
                // Determine overall status
                if (druid.getActiveCount() > 0 || druid.getPoolingCount() > 0) {
                    status.setStatus("HEALTHY");
                } else if (druid.getConnectErrorCount() > 0) {
                    status.setStatus("ERROR");
                } else {
                    status.setStatus("IDLE");
                }
                
                logger.debug("Pool status - Active: {}, Pooling: {}, Errors: {}", 
                    status.getActiveCount(), status.getPoolingCount(), status.getConnectErrorCount());
                    
            } catch (Exception e) {
                logger.error("Error getting pool status", e);
                status.setStatus("UNKNOWN");
            }
        } else {
            status.setStatus("NOT_DRUID");
        }
        
        return status;
    }

    public void resetPoolStats() {
        if (dataSource instanceof DruidDataSource druid) {
            druid.resetStat();
            logger.info("Pool statistics reset");
        }
    }

    public DruidDataSource getDruidDataSource() {
        if (dataSource instanceof DruidDataSource druid) {
            return druid;
        }
        return null;
    }
}
